% Runner schema
% This schema is designed to provide an example of each of the data schema
% components and does not correspond to an existing data set.  Temporal
% extents and frequencies are intended to be illustrative rather than be
% accurate representations of a real data set.

% Entities
item(runner).
item(training-program).
item(coach).
item(training-session).
item(race).

% Variables (attributes)
itemVar(finish-position, race).
itemVar(time, race).
itemVar(experience, coach).
itemVar(miles-run, training-session).
itemVar(training-weather, training-session).
itemVar(age, runner).

% Relations
baseRelated(runner, training-program, one, many).
baseRelated(coach, training-program, one, many).
baseRelated(training-program, training-session, one, many).
baseRelated(runner, race, one, many).

% Entity extents
itemExtent(runner, 3650). % Runners compete for 10 years
itemExtent(coach, 7300).  % Coaches train for 20 years
itemExtent(race, 1).      % Races last one day
itemExtent(training-program, 365).  % Training program lasts full year
itemExtent(training-session, 1).    % Training sessions are 1 day

% Relation frequencies
% Runners begin a new training program each year
relationFrequency(runner, training-program, 365).
% Coaches stay with a training program for the whole year
relationFrequency(coach, training-program, 365).
% The training program requires training every day
relationFrequency(training-program, training-session, 1).
% Runners race six times per year
relationFrequency(runner, race, 60).

% Domain knowledge about possible causes
% Choice of coach does not depend on anything within the schema
setAsTaboo(agg(coach), _).
% A runner's age does not depend on anything within the schema
setAsTaboo(agg(age), _).
% A coach's experience does not depend on anything within the schema
setAsTaboo(agg(experience), _).
% Weather during training does not depend on anything within the schema 
setAsTaboo(agg(training-weather), _).
% Finish position does not depend on training weather
setAsTaboo(agg(finish-position), agg(training-weather)).
% Race time does not depend on training weather
setAsTaboo(agg(time), agg(training-weather)).

