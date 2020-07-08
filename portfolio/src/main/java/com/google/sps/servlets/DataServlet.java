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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Optional; 
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/data")
public class DataServlet extends HttpServlet {

  class Comment {
      private String name;
      private String comment; 
      private long timestamp;

      public Comment(String name, String comment, long timestamp) {
          this.name = name;
          this.comment = comment; 
          this.timestamp = timestamp;
      }
  }

  private List<Comment> comments;


  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    comments = new ArrayList<>();

    int numComments;

    Optional<Integer> maybeLimit = getLimit(request); 
    if (!maybeLimit.isPresent()) {
        System.err.println("The limit input is not a valid number");
        numComments = 0;
    } else {
        numComments = maybeLimit.get();
    }
    
    Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    for (Entity entity : results.asIterable(FetchOptions.Builder.withLimit(numComments))) {
      String name = (String) entity.getProperty("name");
      String text = (String) entity.getProperty("comment");
      long timestamp = (long) entity.getProperty("timestamp");

      Comment comment = new Comment(name, text, timestamp);
      comments.add(comment);
    }

    String json = convertToJson(comments);

    // Send the JSON as the response
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the input from the form. and add to comments array
    String comment = getParameter(request, "text-input", "");
    String name = getParameter(request, "name-input", "");
    String limit = getParameter(request, "limit-input", "");
    long timestamp = System.currentTimeMillis();

    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("comment", comment);
    commentEntity.setProperty("name", name);
    commentEntity.setProperty("timestamp", timestamp);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);

    response.sendRedirect("/thanks.html");
  }

  private String convertToJson(List<Comment> comments) {
    Gson gson = new Gson();
    String json = gson.toJson(comments);
    return json;
  }

 /**
   * @return the request parameter, or the default value if the parameter
   *         was not specified by the client
   */
  private String getParameter(HttpServletRequest request, String name, String defaultValue) {
    String value = request.getParameter(name);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }

  private Optional<Integer> getLimit(HttpServletRequest request) {
    // Get the input from the form.
    String numCommentsString = request.getParameter("limit-input");

    // Convert the input to an optional integer.
    int limit;

    try {
      limit = Integer.parseInt(numCommentsString);
    } catch (NumberFormatException e) {
      return Optional.empty();
    }

    if (limit < 0) {
      return Optional.empty();
    }

    Optional<Integer> numComments = Optional.of(limit); 
    
    return numComments;
  }
}
