/*
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 */
:- multifile(entity(_)).
:- multifile(relationship(_)).
:- multifile(cardinality(_, _, _)).
:- multifile(attr(_, _)).
:- multifile(primaryKey(_, _)).
:- multifile(foreignKey(_, _, _)).

:- multifile(avoidCycle(_)).
:- multifile(categorical(_)).
:- multifile(numBins(_, _)).

:- ensure_loaded('structure-transformation.pl').
:- ensure_loaded('variable-transformation.pl').
:- ensure_loaded('unit-construction.pl').
:- ensure_loaded('utils.pl').
