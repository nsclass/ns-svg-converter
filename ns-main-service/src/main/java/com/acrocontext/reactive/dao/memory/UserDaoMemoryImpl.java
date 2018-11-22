/*
 * Copyright 2017-present, Nam Seob Seo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.acrocontext.reactive.dao.memory;

import com.acrocontext.common.dao.UserDao;
import com.acrocontext.domain.User;
import com.acrocontext.exceptions.GeneralUserNotFound;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Date 12/25/17
 *
 * @author Nam Seob Seo
 */

@Repository
@Profile("dao_memory")
public class UserDaoMemoryImpl implements UserDao {

    private Map<String, User> userMap = new ConcurrentHashMap<>();

    @Override
    public Mono<User> getUser(String email) {
        User user = userMap.computeIfPresent(email, (k, v) -> v);
        if (user != null) {
            return Mono.just(user);
        } else {
            return Mono.empty();
        }
    }

    @Override
    public Mono<User> addUser(User user) {
        try {
            userMap.putIfAbsent(user.getEmail(), user);
            return Mono.just(user);
        } catch (Exception e) {
            return Mono.error(new GeneralUserNotFound(e.getMessage()));
        }
    }

    @Override
    public Mono<User> updateUser(User user) {
        try {
            userMap.put(user.getEmail(), user);
            return Mono.just(user);
        } catch (Exception e) {
            return Mono.error(new GeneralUserNotFound(e.getMessage()));
        }
    }

    public void clearData() {
        Set<String> found = new HashSet<>();

        userMap.forEach((k, v) -> {
            if (!k.equals("admin@admin.com")) {
                found.add(k);
            }
        });

        userMap.keySet().removeAll(found);
    }
}
