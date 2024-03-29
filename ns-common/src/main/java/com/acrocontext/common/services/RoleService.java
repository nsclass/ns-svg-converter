/*
 * Copyright 2017-2023, Nam Seob Seo
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
package com.acrocontext.common.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Date 11/26/17
 *
 * @author Nam Seob Seo
 */
@Service
public class RoleService {

  private final List<String> roles = Arrays.asList("ADMIN", "STAFF", "MEMBER", "LIMITED_MEMBER");

  public List<String> getAllRoles() {
    return roles;
  }

  public List<String> getMapAuthorities(String role) {
    int idx = roles.indexOf(role);
    if (idx >= 0) {
      return roles.subList(idx, roles.size());
    }

    return new ArrayList<>();
  }
}
