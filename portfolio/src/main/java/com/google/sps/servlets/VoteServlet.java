// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.HashMap;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/authorize")
public class VoteServlet extends HttpServlet {

  private String loginStatus;
  private String URL;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    HashMap<String, String> loginMap = new HashMap();

    UserService userService = UserServiceFactory.getUserService();
    if (userService.isUserLoggedIn()) {
      loginStatus = "loggedIn";
      String urlToRedirectToAfterUserLogsOut = "/music.html";
      URL = userService.createLogoutURL(urlToRedirectToAfterUserLogsOut);

    } else {
      loginStatus = "loggedOut";
      String urlToRedirectToAfterUserLogsIn = "/music.html";
      URL = userService.createLoginURL(urlToRedirectToAfterUserLogsIn);
    }

    loginMap.put("loginStatus", loginStatus);
    loginMap.put("URL", URL);

    String json = convertToJson(loginMap);

    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  private String convertToJson(HashMap<String, String> loginMap) {
    Gson gson = new Gson();
    String json = gson.toJson(loginMap);
    return json;
  }
}
