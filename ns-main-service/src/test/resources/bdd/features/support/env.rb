require 'json'
require 'net/http'
require 'uri'
require 'yaml'
require 'cassandra'
require 'rest-client'
require 'json_spec/cucumber'
require 'rspec/expectations'

puts RUBY_VERSION

AfterConfiguration do |config|
  $config = load_environment('config_bdd.yml')
  wait_for_main_app_start

  if $config['cassandra']['active']
    connect_cassandra
  end
end

Before do
  if $config['cassandra']['active']
    setup_cassandra_data($keyspace, $session)
  else
    clear_in_memory_data
  end

  # insert test users
  insert_test_user('/api/v1/users/register', 'read@user.com', 'password')
  clear_token_data
end

After do

end

def clear_in_memory_data
  # clear data when using memory based dao
  admin_login_status = false
  admin_current_password = 'pleasechangepassword'

  (1..120).each {|idx|
    puts "Waiting for logging in with admin (retry: #{idx}) ..."

    unless admin_login_status
      puts admin_current_password
      request_http_login('admin@admin.com', admin_current_password, '/api/v1/login')

      if last_json != nil
        if last_json.code == 200
          admin_login_status = true
        else
          admin_current_password = 'password'
        end

      end
    end

    if admin_login_status
      request_http_with_token('PUT', '/api/v1/admin/clear', get_bearer_token)
      break
    end
  }

end

def clear_token_data
  $http_json_response = nil
  $last_login_token = nil
end

def load_environment(file)
  YAML.load_file(file)
end

def connect_cassandra
  cassandra_cfg = $config['cassandra']
  host_list = [cassandra_cfg['host']]
  cluster = Cassandra.cluster(:hosts => host_list)

  cluster.each_host do |host| # automatically discovers all peers
    puts "Host #{host.ip}: id=#{host.id} datacenter=#{host.datacenter} rack=#{host.rack}"
  end

  # connect server
  $keyspace = cassandra_cfg['keyspace']
  $session = cluster.connect(cassandra_cfg['keyspace'])
end

def setup_cassandra_data(keyspace, session)
  # get all tables
  table_list = get_all_tables(session, keyspace)

  # truncate tables
  truncate_exclude_table_list = ['admin_data']
  truncate_tables(session, table_list, truncate_exclude_table_list)
end

def truncate_tables(session, table_list, excluded_list)

  table_list.each do |table|

    result = excluded_list.bsearch { |x| x == table}
    if result == nil
      # truncate
      query = "truncate #{table}"
      puts "truncate #{table}"
      session.execute(query)
    end
  end
end

def get_all_tables(session, keyspace)
  query = "SELECT table_name FROM system_schema.tables WHERE keyspace_name = '#{keyspace}'"
  result = session.execute(query)

  table_list = Array.new
  result.each do |row|
    # puts "cassandra table: #{row}"
    table_list.push(row['table_name'])
  end

  table_list
end

def wait_for_main_app_start

  # check health check
  admin_login_status = false
  admin_current_password = 'pleasechangepassword'

  (1..120).each {|idx|
    puts "Waiting for ns-main-service to start(retry: #{idx}) ..."

    unless admin_login_status
      puts admin_current_password
      request_http_login('admin@admin.com', admin_current_password, '/api/v1/login')

      if last_json != nil
        if last_json.code == 200
          admin_login_status = true;
        else
          admin_current_password = 'password'
        end

      end
    end

    if admin_login_status
      request_http_with_token('GET', '/actuator/health', get_bearer_token)
      app_status = JSON.parse(last_json.body)
      puts app_status
      if app_status['status'] == 'UP'
        puts 'ns-main-service is up and running'

        # change admin password
        if admin_current_password != 'password'
          request_http_with_token('PATCH',
                                  '/api/v1/users/operations/password',
                                  get_bearer_token,
                                  'form',
                                  'oldpassword=pleasechangepassword&newpassword=password')

          last_json.code.should == 200
        end

        break
      end
    end

    sleep(1.0)
  }
end
