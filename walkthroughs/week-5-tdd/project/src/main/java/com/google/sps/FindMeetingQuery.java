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

package com.google.sps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.ListIterator;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Set;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    ArrayList<TimeRange> potentialTimes = new ArrayList<>();
    potentialTimes.add(TimeRange.WHOLE_DAY);
    Collection<String> meetingGuests = request.getAttendees();
    Collection<String> optionalGuests = request.getOptionalAttendees();
    long meetingDuration = request.getDuration();

    Collection<TimeRange> times = new ArrayList<>();

    ArrayList<TimeRange> badTimes = new ArrayList<>();

    for (Event event: events) {
      TimeRange eventTime = event.getWhen();
      Set<String> guests = event.getAttendees();
      for (String optional: optionalGuests) {
        if (guests.contains(optional)) {
          badTimes.add(eventTime);
        }
      }
      for (String person: meetingGuests) {
        if (guests.contains(person)) {
          fixConflicts(potentialTimes, eventTime);
        }
      }
    }

    ArrayList<TimeRange> copyOfGoodTimes = new ArrayList<TimeRange>(potentialTimes);
    HashMap<TimeRange, Integer> someOptionals = new HashMap<>();
 
    for (TimeRange badTime: badTimes) {
      boolean accounted = false;
      ListIterator<TimeRange> litr = potentialTimes.listIterator();
      while (litr.hasNext()) {
        TimeRange time = litr.next();
        if (badTime.overlaps(time)) {
          litr.remove();
          someOptionals.put(time, 1);
          if (time.start() < badTime.start()) {
            TimeRange frontBlock = TimeRange.fromStartEnd(time.start(), badTime.start(), false);
            litr.add(frontBlock);
          }
          if (badTime.end() < time.end()) {
            TimeRange backBlock = TimeRange.fromStartEnd(badTime.end(), time.end(), false);
            litr.add(backBlock);
          }
        }
        accounted = true; 
      }
      if (!accounted) {
          for (TimeRange deletedTime: someOptionals.keySet()) {
              if (badTime.overlaps(deletedTime)) {
                  int count = someOptionals.get(deletedTime);
                  someOptionals.replace(deletedTime, count + 1);
              }
          }
      }
    }

    if (meetingGuests.isEmpty()) {
      copyOfGoodTimes = potentialTimes;
    }

    ListIterator<TimeRange> litr = potentialTimes.listIterator();
    while (litr.hasNext()) {
      TimeRange time = litr.next(); 
      if (meetingDuration > time.duration()) {
        litr.remove();
      }
    } 

    if (potentialTimes.isEmpty() && !badTimes.isEmpty()) {
      potentialTimes = copyOfGoodTimes;
      ArrayList<TimeRange> leastClashes = new ArrayList<>();
      if (!someOptionals.isEmpty() && !meetingGuests.isEmpty()) {
          Collection<Integer> values = someOptionals.values();
          int min = Collections.min(values); 
          for (TimeRange time: someOptionals.keySet()) {
              if (someOptionals.get(time) == min) {
                  leastClashes.add(time);
              }
          }
      }
      if (!leastClashes.isEmpty()) potentialTimes = leastClashes;
    }

    times = potentialTimes;    
    return times;
  }

  public void fixConflicts(ArrayList<TimeRange> potentialTimes, TimeRange clashTime) {
    ListIterator<TimeRange> litr = potentialTimes.listIterator();
    while (litr.hasNext()) {
      TimeRange time = litr.next();
      if (clashTime.overlaps(time)) {
        litr.remove();
        if (time.start() < clashTime.start()) {
          TimeRange frontBlock = TimeRange.fromStartEnd(time.start(), clashTime.start(), false);
          litr.add(frontBlock);
        }
        if (clashTime.end() < time.end()) {
          TimeRange backBlock = TimeRange.fromStartEnd(clashTime.end(), time.end(), false);
          litr.add(backBlock);
        }
      }
    }
  }
}
