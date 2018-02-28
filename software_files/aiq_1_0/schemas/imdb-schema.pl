/**
 * $Id: imdb-schema.pl 36 2008-04-04 15:29:08Z afast $
 *
 * Part of the open-source AIQUE system
 *   (see LICENSE for copyright and license information).
 *
 */

/* 
 * imdb-schema.pl contains the schema predicates for the imdb database.
 */

%Item declaration.
item(movie).
item(actor).
item(actor-stint).
item(director).
item(director-stint).
item(award).
item(reviewer).
item(review).

%Variable declaration.
itemVar(ds-start-date, director-stint).
itemVar(as-start-date, actor-stint).
itemVar(ds-end-date, director-stint).
itemVar(as-end-date, actor-stint).

itemVar(movie-release-date, movie).
itemVar(receipts, movie).
itemVar(budget, movie).
itemVar(genre, movie).
itemVar(award-date, award).
itemVar(category, award).
itemVar(review-date, review).
itemVar(rating, review).
itemVar(age, actor).

%Relations among items. For now these encode the relations we need to do temporal analysis.
baseRelated(movie, director-stint, one, many).
baseRelated(movie, actor-stint, one, many).
baseRelated(movie, review, one, many).
baseRelated(movie, award, one, many).
baseRelated(award, director, many, one).
baseRelated(award, actor, many, one).
baseRelated(reviewer, review, one, many).
baseRelated(director, director-stint, one, many).
baseRelated(actor, actor-stint, one, many).

%Temporal Declarations

%Possible base items.
itemExtent(movie, 7300). %movies last 20 years
itemExtent(reviewer, 3650). %reviewers rate for 10 years
itemExtent(director, 7300). %directors work for 20 years
itemExtent(actor, 7300). %actors act for 20 years

relationFrequency(director, director-stint, 730). %directors direct a new movie every 2 years.
relationFrequency(director, award, 365). %directors get awards once a year.
relationFrequency(actor, actor-stint, 365). %actors are in a new movie every year.
relationFrequency(actor, award, 365). %actors get awards once a year.
relationFrequency(reviewer, review, 7). %reviewers rate a movie about once a week.
relationFrequency(movie, review, 1). %movies are constantly getting new ratings.
relationFrequency(movie, award, 6935). %movies get an award about a year after release and keep it.
relationFrequency(movie, director-stint, 7300). %movies never change their directors after release.
relationFrequency(movie, actor-stint, 7300). %movies never change their actors after release.

%Define causes
setAsTaboo(exists(award), _).
setAsTaboo(award-date, _).
setAsTaboo(movie-release-date, _).
setAsTaboo(exists(movie), _).
