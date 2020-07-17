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

  // The following function returns a Collection<TimeRange> of all possible times for a meeting to occur given 
  // a Collection<Event> of all known events for that day, and a MeetingRequest. Each event has a title, list of guests attending
  // this event, and a TimeRange for the event(which includes the start potentialTime and duration of the event). Each MeetingRequest 
  // has a title, duration requested for the meeting, and a list of guests invited to this meeting. This funtion performs the task by 
  // starting with the whole day, and then continuosly removing blocks of potentialTime that clash with any other known event, if there is a
  // guest for the MeetingRequest who has to attend this event.

  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    ArrayList<TimeRange> potentialMeetingTimes = new ArrayList<>();
    // Add the whole day to potential meeting times
    potentialMeetingTimes.add(TimeRange.WHOLE_DAY);

    Collection<String> mandatoryMeetingGuests = request.getAttendees();
    Collection<String> optionalMeetingGuests = request.getOptionalAttendees();
    long meetingDuration = request.getDuration();
    Collection<TimeRange> finalPossibleTimes = new ArrayList<>();

    ArrayList<TimeRange> unavailableTimesForOptionalGuests = new ArrayList<>();
    
    // Check if there are any meeting guests in the event guest list, and if there are, remove the block of time 
    // for the event from the potential meeting times 
    for (Event event: events) {
      TimeRange eventTime = event.getWhen();
      Set<String> eventGuests = event.getAttendees();
      // if there are any optional guests attending this event, add the time to the list of unavailable times for optional guests 
      for (String optionalMeetingGuest: optionalMeetingGuests) {
        if (eventGuests.contains(optionalMeetingGuest)) {
          unavailableTimesForOptionalGuests.add(eventTime);
        }
      }
      for (String mandatoryMeetingGuest: mandatoryMeetingGuests) {
        if (eventGuests.contains(mandatoryMeetingGuest)) {
          fixSchedulingConflicts(potentialMeetingTimes, eventTime);
        }
      }
    }
    
    // Make a copy of good meeting times we have at this point for mandatory guests as a backup, in case scheduling is impossible with optional guests
    ArrayList<TimeRange> copyOfGoodMeetingTimes = new ArrayList<TimeRange>(potentialMeetingTimes);
    // Make a hashmap of all eliminated times and frequency of elimination, to optimize scheduling with the most optional guests 
    HashMap<TimeRange, Integer> eliminatedTimes = new HashMap<>();
 
    for (TimeRange unavailableTime: unavailableTimesForOptionalGuests) {
      // boolean to check if an unavailable time has already been deleted
      boolean timeAccounted = false;
      // create a list iterator for potential meeting times so it can be modified while iterating 
      ListIterator<TimeRange> litr = potentialMeetingTimes.listIterator();
      while (litr.hasNext()) {
        TimeRange potentialTime = litr.next();
        if (unavailableTime.overlaps(potentialTime)) {
          litr.remove();
          // put unavailable time in eliminated times hasmap and assign frequency of 1 first time it is deleted
          eliminatedTimes.put(potentialTime, 1);
          if (potentialTime.start() < unavailableTime.start()) {
            TimeRange frontBlock = TimeRange.fromStartEnd(potentialTime.start(), unavailableTime.start(), false);
            litr.add(frontBlock);
          }
          if (unavailableTime.end() < potentialTime.end()) {
            TimeRange backBlock = TimeRange.fromStartEnd(unavailableTime.end(), potentialTime.end(), false);
            litr.add(backBlock);
          }
        }
        timeAccounted = true; 
      }
      // if the uunavailable time is not deleted, check if it has previously been deleted and inrease frequency by 1
      if (!timeAccounted) {
          for (TimeRange deletedTime: eliminatedTimes.keySet()) {
              if (unavailableTime.overlaps(deletedTime)) {
                  int count = eliminatedTimes.get(deletedTime);
                  eliminatedTimes.replace(deletedTime, count + 1);
              }
          }
      }
    }
    
    // if there are no mandatory meeting guests, then we want to schedule only around optional guests
    if (mandatoryMeetingGuests.isEmpty()) {
      copyOfGoodMeetingTimes = potentialMeetingTimes;
    }

    // if there is potential meeting time has a shorter duration than the meeting request, then delete that potential time because it doesn't work
    ListIterator<TimeRange> litr = potentialMeetingTimes.listIterator();
    while (litr.hasNext()) {
      TimeRange potentialTime = litr.next(); 
      if (meetingDuration > potentialTime.duration()) {
        litr.remove();
      }
    } 

    // if it was impossible to find a time to work for all mandatory guests and all optional guests, then get the optimized times where the highest
    // number of optional guests can attend
    if (potentialMeetingTimes.isEmpty() && !unavailableTimesForOptionalGuests.isEmpty()) {
      potentialMeetingTimes = copyOfGoodMeetingTimes;
      // array to keep timeranges where the most optional guests can attend
      ArrayList<TimeRange> timesWithLeastClashes = new ArrayList<>();
      if (!eliminatedTimes.isEmpty() && !mandatoryMeetingGuests.isEmpty()) {
          // gets all frequencies of the deleted times
          Collection<Integer> counts = eliminatedTimes.values();
          // pick the minimum frequency, because that means this is the time where the least number of optional guests have clashes 
          int min = Collections.min(counts); 
          for (TimeRange potentialTime: eliminatedTimes.keySet()) {
              if (eliminatedTimes.get(potentialTime) == min) {
                  timesWithLeastClashes.add(potentialTime);
              }
          }
      }
      // if tyhere is an optimized solution, then use those times 
      if (!timesWithLeastClashes.isEmpty()) potentialMeetingTimes = timesWithLeastClashes;
    }

    finalPossibleTimes = potentialMeetingTimes;    
    return finalPossibleTimes;
  }

  // Given an ArrayList of potential meeting times and a clashing time, this function returns times around this clash time, eliminating 
  // the conflict

  public void fixSchedulingConflicts(ArrayList<TimeRange> potentialMeetingTimes, TimeRange clashTime) {
    // creates a modifiable iterable for the list of potential meeting times 
    ListIterator<TimeRange> litr = potentialMeetingTimes.listIterator();
    while (litr.hasNext()) {
      TimeRange potentialTime = litr.next();
      if (clashTime.overlaps(potentialTime)) {
        //  remove any time that ovrlaps with the clash time 
        litr.remove();
        if (potentialTime.start() < clashTime.start()) {
        // if there is time available before the start of the conflict time, add to the list of potential times 
          TimeRange frontBlock = TimeRange.fromStartEnd(potentialTime.start(), clashTime.start(), false);
          litr.add(frontBlock);
        }
        // if there is time available after the end of the conflict time, add to the list of potential times 
        if (clashTime.end() < potentialTime.end()) {
          TimeRange backBlock = TimeRange.fromStartEnd(clashTime.end(), potentialTime.end(), false);
          litr.add(backBlock);
        }
      }
    }
  }
}
