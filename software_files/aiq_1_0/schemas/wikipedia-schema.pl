/**
 * $Id: wikipedia-schema.pl 36 2008-04-04 15:29:08Z afast $
 *
 * Part of the open-source AIQUE system
 *   (see LICENSE for copyright and license information).
 *
 */

/* 
 * wikipedia-schema.pl contains the schema predicates for the wikipedia database.
 */

%Item declaration.
item(username).
item(page).
item(edit).
item(username-log).
item(page-log).


%Variable declaration.
itemVar(ed-date, edit).
itemVar(ul-date, username-log).
%itemVar(ul-iscreation, username-log).
%itemVar(ul-isrename, username-log).
%itemVar(ul-isrights, username-log).

itemVar(pl-date, page-log).
%itemVar(pl-isprotection, page-log).
%itemVar(pl-isdeletion, page-log).
%itemVar(pl-ismove, page-log).

%itemVar(namespace, page).
itemVar(title, page).
%itemVar(restrictions, page).

%Relations among items. For now these encode the relations we need to do temporal analysis.
baseRelated(username, edit, one, many).
baseRelated(edit, page, many, one).
baseRelated(username-log, username, many, one).
baseRelated(page-log, page, many, one).

%Temporal Declarations

itemExtent(username, 3650).
itemExtent(page, 3650).

relationFrequency(username, username-log, 1).
relationFrequency(username, edit, 1).

relationFrequency(page, page-log, 1).
relationFrequency(page, edit, 1).

%Define causes
setAsTaboo(exists(username-log), _).
setAsTaboo(agg(ul-date), _).
setAsTaboo(exists(page-log), _).
setAsTaboo(agg(pl-date), _).

