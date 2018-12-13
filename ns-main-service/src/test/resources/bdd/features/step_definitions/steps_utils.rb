require 'rest-client'
require 'json_spec/cucumber'
require 'json'

def last_json
  $http_json_response
end

def request_http_with_token(method, path, bearer_token = nil, content_type = nil, content = nil)
  host = $config['services']['ns-main-service']['host']
  port = $config['services']['ns-main-service']['port']
  ssl = $config['services']['ns-main-service']['ssl']

  if ssl
    url = "https://#{host}:#{port}#{path}"
  else
    url = "http://#{host}:#{port}#{path}"
  end

  url = replace_memorized_variables(url, true)
  if content != nil
    content = replace_memorized_variables(content, false)
  end

  request_method = create_request_method(method)
  content_type_value = nil
  if content_type != nil
      content_type_value = get_content_type(content_type)
  end

  begin
    $http_json_response = RestClient::Request.execute(method: request_method,
                                                      url: url,
                                                      :verify_ssl => false,
                                                      headers: {
                                                          :Authorization => bearer_token,
                                                          content_type: content_type_value,
                                                          :accept => :json
                                                      },
                                                      payload: content)

  rescue RestClient::ServerBrokeConnection => error
    puts error
    $http_json_response = nil
    $last_login_token = nil
  rescue RestClient::ExceptionWithResponse => error
    puts error
    $http_json_response = error.response
    $last_login_token = nil
  rescue => error
    puts error
    $http_json_response = nil
    $last_login_token = nil
  end
end

def create_request_method(method)

  request_method =
      case method
      when 'GET' then
        :get
      when 'OPTIONS' then
        :options
      when 'PATCH' then
        :patch
      when 'POST' then
        :post
      when 'PUT' then
        :put
      when 'DELETE' then
        :delete
      else
        :get
      end

  request_method
end

def get_bearer_token
  bearer_token = nil
  if $last_login_token != nil
    bearer_token = 'Bearer ' + $last_login_token
  end
  bearer_token
end

def get_content_type(content_type)
  content_type_value = :json

  if content_type == 'form'
    content_type_value = 'application/x-www-form-urlencoded'
  elsif content_type == 'json'
    content_type_value = :json
  elsif content_type == 'text'
    content_type_value = 'application/text'
  end

  content_type_value

end

def request_http_login(username, password, path)
  host = $config['services']['ns-main-service']['host']
  port = $config['services']['ns-main-service']['port']
  ssl = $config['services']['ns-main-service']['ssl']

  if ssl
    url = "https://#{host}:#{port}#{path}"
  else
    url = "http://#{host}:#{port}#{path}"
  end

  request_method = :post
  content_type_value = 'application/x-www-form-urlencoded'

  payload = "username=#{username}&password=#{password}"

  begin
    $http_json_response = RestClient::Request.execute(method: request_method,
                                                      url: url,
                                                      :verify_ssl => false,
                                                      headers: {
                                                          content_type: content_type_value,
                                                          :accept => :json
                                                      },
                                                      payload: payload)

    $last_login_token = JSON.parse($http_json_response)['token']

  rescue RestClient::ServerBrokeConnection => error
    puts error
    $http_json_response = nil
    $last_login_token = nil
  rescue RestClient::ExceptionWithResponse => error
    puts error
    $http_json_response = error.response
    $last_login_token = nil
  rescue => error
    puts error
    $http_json_response = nil
    $last_login_token = nil
  end

end

def request_http_save(method, path, content_type, content)
  host = $config['services']['ns-main-service']['host']
  port = $config['services']['ns-main-service']['port']
  ssl = $config['services']['ns-main-service']['ssl']

  if ssl
    url = "https://#{host}:#{port}#{path}"
  else
    url = "http://#{host}:#{port}#{path}"
  end


  request_method =
      case method
      when 'GET' then
        :get
      when 'POST' then
        :post
      when 'PUT' then
        :put
      when 'DELETE' then
        :delete
      else
        :post
      end


  content_type_value =
      case content_type
      when 'form' then
        'application/x-www-form-urlencoded'
      when 'json' then
        :json
      when 'text' then
        'application/text'
      else
        :json
      end

  begin
    $http_json_response = RestClient::Request.execute(method: request_method,
                                                      url: url,
                                                      :verify_ssl => false,
                                                      headers: {
                                                          content_type: content_type_value,
                                                          :accept => :json
                                                      },
                                                      payload: content)
  rescue RestClient::ServerBrokeConnection => error
    puts error
    $http_json_response = nil
    $last_login_token = nil
  rescue RestClient::ExceptionWithResponse => error
    puts error
    $http_json_response = error.response
    $last_login_token = nil
  rescue => error
    puts error
    $http_json_response = nil
    $last_login_token = nil
  end
end


# response = RestClient::Request.new({
#                                        method: :post,
#                                        url: 'https://xyz,
#       user: 'someone',
#       password: 'mybirthday',
#       payload: { post_this: 'some value', post_that: 'other value' },
#       headers: { :accept => :json, content_type: :json }
#     }).execute do |response, request, result|
#       case response.code
#       when 400
#         [ :error, parse_json(response.to_str) ]
#       when 200
#         [ :success, parse_json(response.to_str) ]
#       else
#         fail "Invalid response #{response.to_str} received."
#       end
#     end

def insert_test_user(url, username, password)

  host = $config['services']['ns-main-service']['host']
  port = $config['services']['ns-main-service']['port']
  ssl = $config['services']['ns-main-service']['ssl']

  payload = {
      :email => "#{username}",
      :password => "#{password}",
      :passwordConfirm => "#{password}"}.to_json

  begin
    if ssl
      path = "https://#{host}:#{port}" + url
      response = RestClient::Request.execute(method: :post,
                                             url: path,
                                             :verify_ssl => false,
                                             headers: {
                                                 content_type: :json,
                                                 :accept => :json
                                             },
                                             payload: payload)
    else
      path = "http://#{host}:#{port}" + url
      response = RestClient.post path, payload,
                                 {content_type: :json, accept: :json}
    end

    response.code.should == 200

    $http_json_response = response

    puts "A user: #{username} has been added"
  rescue RestClient::ServerBrokeConnection => error
    puts "Failed to add a user #{error}"
    $http_json_response = nil
    $last_login_token = nil
  rescue RestClient::ExceptionWithResponse => error
    puts "Failed to add a user #{error}"
    $http_json_response = error.response
    $last_login_token = nil
  rescue => error
    puts "Failed to add a user #{error}"
    $http_json_response = nil
    $last_login_token = nil

    # response = error.response
    # full_error = "#{error}\nHTTP response: #{response.body}\n#{response.headers}"
    # raise RSpec::Expectations::ExpectationNotMetError.new(full_error)
  end

end



# find all the unique field names in a JSON document
def get_unique_field_names(json_doc, fields_set = SortedSet.new([]))

  json_doc.each{|k,v| fields_set.add(k)} if json_doc.is_a?(Hash)
  json_doc.each{|k,v| get_unique_field_names(json_doc[k],fields_set)} if json_doc.is_a?(Hash)
  json_doc.each {|doc| get_unique_field_names(doc, fields_set)} if json_doc.is_a?(Array)
  fields_set
end

# removes the field names from the JSON doc that are not in the allowed list
def remove_field_names(json_doc, fields_set)

  json_doc.reject!{|k,v| not fields_set.include?(k)} if json_doc.is_a?(Hash)
  json_doc.each{|k,v| remove_field_names(json_doc[k],fields_set)} if json_doc.is_a?(Hash)
  json_doc.each {|doc| remove_field_names(doc, fields_set)} if json_doc.is_a?(Array)

  json_doc
end


# takes a string and replaces all references to JsonSpec memory variables with actual values
def replace_memorized_variables(string_value, remove_quotes = true)
  # if JsonSpec variables are present, replace any potential references to them in the URL
  JsonSpec.memory.each do |doc|
    var_name = doc[0]
    # doc[1] is stored with quotes "....." so we have to remove them if told to (usually yes)
    var_value = doc[1]
    var_value = var_value.gsub('"',"") if remove_quotes
    string_value = string_value.gsub("%{#{var_name}}",var_value) if string_value != nil
  end

  string_value
end

# removes the field names from the JSON doc in the list
def remove_field_names_exclusive(json_doc, fields_set)

  json_doc.reject!{|k,v| fields_set.include?(k)} if json_doc.is_a?(Hash)
  json_doc.each{|k,v| remove_field_names_exclusive(json_doc[k],fields_set)} if json_doc.is_a?(Hash)
  json_doc.each {|doc| remove_field_names_exclusive(doc, fields_set)} if json_doc.is_a?(Array)

  json_doc
end
