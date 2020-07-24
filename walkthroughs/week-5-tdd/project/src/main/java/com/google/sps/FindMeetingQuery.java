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

/**
* The FindingMeetingQuery is a container class that is able to find available meeting times for a particualr requested meeting
*/

public final class FindMeetingQuery {

  /**
  * Finds potential meeting times by first starting with the whole day as a postential time, and then removing blocks of 
  * times where another event with a common guest is ongoing
  * 
  * @param  events  a Collection<Event> each with an event title, list of guests and Timerange of event
  * @param  request a MeetingRequest with lists of mandatory and otpional guests, as well as duration for the meeting
  * @return      Collection<TimeRange> of possible times the requested meeting could occur
  */  

  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    ArrayList<TimeRange> potentialMeetingTimes = new ArrayList<>();
    potentialMeetingTimes.add(TimeRange.WHOLE_DAY);

    Collection<String> mandatoryMeetingGuests = request.getAttendees();
    Collection<String> optionalMeetingGuests = request.getOptionalAttendees();
    long meetingDuration = request.getDuration();
    Collection<TimeRange> finalPossibleTimes = new ArrayList<>();

    ArrayList<TimeRange> unavailableTimesForOptionalGuests = new ArrayList<>();
    
    // Check if there are any meeting guests in the event guest list, and if there are, remove the block of time 
    // for the event from the potential meeting times 
    for (Event event : events) {
      TimeRange eventTime = event.getWhen();
      Set<String> eventGuests = event.getAttendees();
      for (String mandatoryMeetingGuest: mandatoryMeetingGuests) {
        if (eventGuests.contains(mandatoryMeetingGuest)) {
          removeConflictTimeFromPotentialTimes(potentialMeetingTimes, eventTime);
        }
      }
      for (String optionalMeetingGuest: optionalMeetingGuests) {
        if (eventGuests.contains(optionalMeetingGuest)) {
          unavailableTimesForOptionalGuests.add(eventTime);
        }
      }
    }
    
    ArrayList<TimeRange> bestPotentialTimesSoFar = new ArrayList<TimeRange>(potentialMeetingTimes);
    // Make a hashmap of all eliminated times and frequency of elimination, to optimize scheduling with the most optional guests 
    HashMap<TimeRange, Integer> eliminatedTimes = new HashMap<>();
 
    for (TimeRange unavailableTime: unavailableTimesForOptionalGuests) {
      boolean unavailableTimeAlreadyDeleted = false;
      ListIterator<TimeRange> potentialTimesIterator = potentialMeetingTimes.listIterator();
      while (potentialTimesIterator.hasNext()) {
        TimeRange potentialTime = potentialTimesIterator.next();
        if (unavailableTime.overlaps(potentialTime)) {
          potentialTimesIterator.remove();
          unavailableTimeAlreadyDeleted = true;
          // Put unavailable time in eliminated times hashmap and assign frequency of 1 first time it is deleted
          eliminatedTimes.put(potentialTime, 1);
          if (potentialTime.start() < unavailableTime.start()) {
          // If there is time available before the start of the conflict time, add to the list of potential times 
            TimeRange timeBeforeConflict = TimeRange.fromStartEnd(potentialTime.start(), unavailableTime.start(), false);
            potentialTimesIterator.add(timeBeforeConflict);
          }
          // If there is time available after the end of the conflict time, add to the list of potential times 
          if (unavailableTime.end() < potentialTime.end()) {
            TimeRange timeAfterConflict = TimeRange.fromStartEnd(unavailableTime.end(), potentialTime.end(), false);
            potentialTimesIterator.add(timeAfterConflict);
          }
        } 
      }
      // If the uunavailable time is not deleted, check if it has previously been deleted and increase frequency by 1
      if (!unavailableTimeAlreadyDeleted) {
          for (TimeRange deletedTime: eliminatedTimes.keySet()) {
              if (unavailableTime.overlaps(deletedTime)) {
                  int count = eliminatedTimes.get(deletedTime);
                  eliminatedTimes.replace(deletedTime, count + 1);
              }
          }
      }
    }
    
    // If there are no mandatory meeting guests, then we want to schedule only around optional guests
    if (mandatoryMeetingGuests.isEmpty()) {
      bestPotentialTimesSoFar = potentialMeetingTimes;
    }

    // If there is potential meeting time has a shorter duration than the meeting request, then delete that potential time because it doesn't work
    ListIterator<TimeRange> potentialTimesIterator = potentialMeetingTimes.listIterator();
    while (potentialTimesIterator.hasNext()) {
      TimeRange potentialTime = potentialTimesIterator.next(); 
      if (meetingDuration > potentialTime.duration()) {
        potentialTimesIterator.remove();
      }
    } 

    // If it was impossible to find a time to work for all mandatory guests and all optional guests, then get the optimized times where the highest
    // number of optional guests can attend
    if (potentialMeetingTimes.isEmpty() && !unavailableTimesForOptionalGuests.isEmpty()) {
      potentialMeetingTimes = bestPotentialTimesSoFar;
      // Array to keep timeranges where the most optional guests can attend
      ArrayList<TimeRange> timesWithLeastClashes = new ArrayList<>();
      if (!eliminatedTimes.isEmpty() && !mandatoryMeetingGuests.isEmpty()) {
          // Gets all frequencies of the deleted times
          Collection<Integer> counts = eliminatedTimes.values();
          // Pick the minimum frequency, because that means this is the time where the least number of optional guests have clashes 
          int min = Collections.min(counts); 
          for (TimeRange potentialTime: eliminatedTimes.keySet()) {
              if (eliminatedTimes.get(potentialTime) == min) {
                  timesWithLeastClashes.add(potentialTime);
              }
          }
      }
      // If there is an optimized solution, then use those times 
      if (!timesWithLeastClashes.isEmpty()) potentialMeetingTimes = timesWithLeastClashes;
    }

    finalPossibleTimes = potentialMeetingTimes;    
    return finalPossibleTimes;
  }


  /**
  * Resolves conflicts by removing unavailable times from potential times, and adding times before and after conflict
  * 
  * @param  potentialMeetingTimes  a Collection of all known potential meeting times 
  * @param  conflictingTime a TimeRange where another event is going on 
  */  

  public void removeConflictTimeFromPotentialTimes(ArrayList<TimeRange> potentialMeetingTimes, TimeRange conflictingTime) {
    ListIterator<TimeRange> potentialTimesIterator = potentialMeetingTimes.listIterator();
    while (potentialTimesIterator.hasNext()) {
      TimeRange potentialTime = potentialTimesIterator.next();
      if (conflictingTime.overlaps(potentialTime)) {
        //  Remove any time that overlaps with the clash time 
        potentialTimesIterator.remove();
        if (potentialTime.start() < conflictingTime.start()) {
          TimeRange timeBeforeConflict = TimeRange.fromStartEnd(potentialTime.start(), conflictingTime.start(), false);
          potentialTimesIterator.add(timeBeforeConflict);
        }
        if (conflictingTime.end() < potentialTime.end()) {
          TimeRange timeAfterConflict = TimeRange.fromStartEnd(conflictingTime.end(), potentialTime.end(), false);
          potentialTimesIterator.add(timeAfterConflict);
        }
      }
    }
  }
}
