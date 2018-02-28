/**
 * $Id: control-group-design.pl 109 2008-08-13 16:13:42Z afast $
 *
 * Part of the open-source AIQUE system
 *   (see LICENSE for copyright and license information).
 *
 */

/* 
 * Definition for the non-equivalent control group design with temporal events as treatments.
 *
 * Requires core-predicates and a schema file to be consulted prior to running.
 */

%Apply design.
controlGroupDesign(CauseItem, EffectItem, CauseVar, EffectVar) :- 
	% Type Checking 
	stream(BaseItem, CauseItem, CauseStream, CauseStreamItems),
	stream(BaseItem, EffectItem, EffectStream, EffectStreamItems),

%	write('CauseStreamItems: '), write(CauseStreamItems), nl,
%	write('EffectStreamItems: '), write(EffectStreamItems), nl,

	CauseItem \== EffectItem, 
	
	% Streams cannot overlap except for the base item.
	delete(CauseStreamItems, BaseItem, CSI),
	delete(EffectStreamItems, BaseItem, ESI),
	noIntersection(CSI, ESI),

	% Schema Checking
	varOf(CauseVar, CauseStream), varOf(EffectVar, EffectStream),

	%Temporal Checking
	temporalPathFrequency(CauseStreamItems, CauseFreq),
	temporalPathFrequency(EffectStreamItems, EffectFreq),

	itemExtent(BaseItem, BaseItemExtent),
	BaseItemExtent >= CauseFreq, %Base static wrt dynamic items.
	BaseItemExtent >= EffectFreq,
	EffectFreq =< CauseFreq, %In a 1:M relationship, if this is true then many effects happen within a single cause.
	
	% Common Causes? and Assoc.
	possibleCauseOf(CauseVar, EffectVar, EffectStream),
	noCommonCauses(CauseVar, EffectVar, CauseStream, EffectStream), 
	associated(CauseVar,EffectVar, CauseStream, EffectStream).
	%nl, write('BaseItem: '), 
	%write(BaseItem), nl.
