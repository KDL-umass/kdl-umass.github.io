/*
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * unit-construction.pl contains the necessary rules to identify
   pairs of paths with common base entities to be used as design units.
 */
 
unitAll(BaseItem, Target1, Path1, Card1, Var1, Target2, Path2, Card2, Var2, HopThresh1, HopThresh2) :-		
	
	(
		% first entity case - singleton path + singleton path (ESS)
		entity(BaseItem), BaseEntity = BaseItem,			
		Target1 = BaseEntity, singletonPathVar(Var1, Target1), Card1 = one, Path1 = [Target1], 
		Target2 = BaseEntity, singletonPathVar(Var2, Target2), Card2 = one, Path2 = [Target2],
		Var1 \== Var2;
		
		% second entity case - entity path + singleton path (EES)
		entity(BaseItem), BaseEntity = BaseItem,
		entityPathVar(BaseEntity, Target1, Var1, Card1, Path1, HopThresh1),
		Target2 = BaseEntity, singletonPathVar(Var2, Target2), Card2 = one, Path2 = [Target2],
		Var1 \== Var2;

		% third entity case - relationship path + singleton path (ERS)
		entity(BaseItem), BaseEntity = BaseItem,
		relationshipPathVar(BaseEntity, Target1, Var1, Card1, Path1, HopThresh1),
		Target2 = BaseEntity, singletonPathVar(Var2, Target2), Card2 = one, Path2 = [Target2],
		Var1 \== Var2;
		
		% fourth entity case - singleton path + entity path (ESE)
		entity(BaseItem), BaseEntity = BaseItem,
		Target1 = BaseEntity, singletonPathVar(Var1, Target1), Card1 = one, Path1 = [Target1],
		entityPathVar(BaseEntity, Target2, Var2, Card2, Path2, HopThresh2),
		Var1 \== Var2;
		
		% fifth entity case - singleton path + relationship path (ESR)
		entity(BaseItem), BaseEntity = BaseItem,
		Target1 = BaseEntity, singletonPathVar(Var1, Target1), Card1 = one, Path1 = [Target1],
		relationshipPathVar(BaseEntity, Target2, Var2, Card2, Path2, HopThresh2),
		( % for structural outcome, the target relationship cannot appear more than once
		  % otherwise implies that relationship must exist prior to its existence
			Var2 \== Target2;
			Var2 == Target2, numOccurrences(Target2, Path2, R), R =< 1
		),
		Var1 \== Var2;
		
		% sixth entity case - entity path + entity path (EEE)
		entity(BaseItem), BaseEntity = BaseItem,
		entityPathVar(BaseEntity, Target1, Var1, Card1, Path1, HopThresh1),
		entityPathVar(BaseEntity, Target2, Var2, Card2, Path2, HopThresh2),
		Var1 \== Var2;
		
		% seventh entity case - entity path + relationship path (EER)
		entity(BaseItem), BaseEntity = BaseItem,
		entityPathVar(BaseEntity, Target1, Var1, Card1, Path1, HopThresh1),
		relationshipPathVar(BaseEntity, Target2, Var2, Card2, Path2, HopThresh2),
		( % for structural outcome, the target relationship cannot appear more than once
		  % otherwise implies that relationship must exist prior to its existence
		  % Also, target relationship cannot appear in the treatment path
			Var2 \== Target2;
			Var2 == Target2, numOccurrences(Target2, Path2, R), R =< 1, not(memberchk(Target2, Path1))
		),
		Var1 \== Var2;
		
		% eighth entity case - relationship path + entity path (ERE)
		entity(BaseItem), BaseEntity = BaseItem,
		relationshipPathVar(BaseEntity, Target1, Var1, Card1, Path1, HopThresh1),
		entityPathVar(BaseEntity, Target2, Var2, Card2, Path2, HopThresh2),
		Var1 \== Var2;
		
		% ninth entity case - relationship path + relationship path (ERR)
		entity(BaseItem), BaseEntity = BaseItem,
		relationshipPathVar(BaseEntity, Target1, Var1, Card1, Path1, HopThresh1),
		relationshipPathVar(BaseEntity, Target2, Var2, Card2, Path2, HopThresh2),
		( % for structural outcome, the target relationship cannot appear more than once
		  % otherwise implies that relationship must exist prior to its existence
		  % Also, target relationship cannot appear in the treatment path
			Var2 \== Target2;
			Var2 == Target2, numOccurrences(Target2, Path2, R), R =< 1, not(memberchk(Target2, Path1))
		),
		Var1 \== Var2;

		% first relationship case - singleton path + singleton path (RSS)
		relationship(BaseItem), BaseRelationship = BaseItem,			
		Target1 = BaseRelationship, singletonPathRelationshipVar(Var1, Target1), Card1 = one, Path1 = [Target1],
			% relationship existence is trivially a precondition of its attributes
			Var1 \== Target1,
		Target2 = BaseRelationship, singletonPathRelationshipVar(Var2, Target2), Card2 = one, Path2 = [Target2],
			% relationship attributes cannot cause its existence
			Var2 \== Target2,
		Var1 \== Var2;

		% second relationship case - entity path + singleton path (RES)
		relationship(BaseItem), BaseRelationship = BaseItem,
		entityPathRelationshipVar(BaseRelationship, Target1, Var1, Card1, Path1, HopThresh1),
		Target2 = BaseRelationship, singletonPathRelationshipVar(Var2, Target2), Card2 = one, Path2 = [Target2],
		( % If outcome is relationship existence, then it cannot appear in the treatment path (except for the base)
			Var2 \== Target2;
			Var2 == Target2, numOccurrences(Target2, Path1, R), R =< 1
		),
		Var1 \== Var2;

		% third relationship case - relationship path + singleton path (RRS)
		relationship(BaseItem), BaseRelationship = BaseItem,
		relationshipPathRelationshipVar(BaseRelationship, Target1, Var1, Card1, Path1, HopThresh1),
		Target2 = BaseRelationship, singletonPathRelationshipVar(Var2, Target2), Card2 = one, Path2 = [Target2],
		( % If outcome is relationship existence, then it cannot appear in the treatment path (except for the base)
			Var2 \== Target2;
			Var2 == Target2, numOccurrences(Target2, Path1, R), R =< 1
		),		
		Var1 \== Var2;

		%fourth relationship case - singleton path + entity path (RSE)
		relationship(BaseItem), BaseRelationship = BaseItem,
		Target1 = BaseRelationship, singletonPathRelationshipVar(Var1, Target1), Card1 = one, Path1 = [Target1],
		entityPathRelationshipVar(BaseRelationship, Target2, Var2, Card2, Path2, HopThresh2),
		Var1 \== Var2;

		%fifth relationship case - entity path + entity path (REE)
		relationship(BaseItem), BaseRelationship = BaseItem,
		entityPathRelationshipVar(BaseRelationship, Target1, Var1, Card1, Path1, HopThresh1),
		entityPathRelationshipVar(BaseRelationship, Target2, Var2, Card2, Path2, HopThresh2),
		Var1 \== Var2;

        %sixth relationship case - relationship path + entity path (RRE)
		relationship(BaseItem), BaseRelationship = BaseItem,
        relationshipPathRelationshipVar(BaseRelationship, Target1, Var1, Card1, Path1, HopThresh1),
        entityPathRelationshipVar(BaseRelationship, Target2, Var2, Card2, Path2, HopThresh2),
		Var1 \== Var2;

		%seventh relationship case - singleton path + relationship path (RSR)
		relationship(BaseItem), BaseRelationship = BaseItem,
		Target1 = BaseRelationship, singletonPathRelationshipVar(Var1, Target1), Card1 = one, Path1 = [Target1],
		relationshipPathRelationshipVar(BaseRelationship, Target2, Var2, Card2, Path2, HopThresh2),
		( % for structural outcome, the target relationship cannot appear more than once
		  % otherwise implies that relationship must exist prior to its existence
			Var2 \== Target2;
			Var2 == Target2, numOccurrences(Target2, Path2, R), R =< 1
		),
		Var1 \== Var2;

        %eighth relationship case - entity path + relationship path (RER)
        relationship(BaseItem), BaseRelationship = BaseItem,
		entityPathRelationshipVar(BaseRelationship, Target1, Var1, Card1, Path1, HopThresh1),
		relationshipPathRelationshipVar(BaseRelationship, Target2, Var2, Card2, Path2, HopThresh2),
		( % for structural outcome, the target relationship cannot appear more than once
		  % otherwise implies that relationship must exist prior to its existence
		  % Also, target relationship cannot appear in the treatment path
			Var2 \== Target2;
			Var2 == Target2, numOccurrences(Target2, Path2, R), R =< 1, not(memberchk(Target2, Path1))
		),
        Var1 \== Var2;

        %ninth relationship case - relationship path + relationship path (RRR)
        relationship(BaseItem), BaseRelationship = BaseItem,
        relationshipPathRelationshipVar(BaseRelationship, Target1, Var1, Card1, Path1, HopThresh1),
        relationshipPathRelationshipVar(BaseRelationship, Target2, Var2, Card2, Path2, HopThresh2),
		( % for structural outcome, the target relationship cannot appear more than once
		  % otherwise implies that relationship must exist prior to its existence
		  % Also, target relationship cannot appear in the treatment path
			Var2 \== Target2;
			Var2 == Target2, numOccurrences(Target2, Path2, R), R =< 1, not(memberchk(Target2, Path1))
		),
        Var1 \== Var2
	),

    % Additional constraints

    % Make sure paths don't start out the same (after the base item)
    notStartingOverlap(Path1, Path2),
    
    % Check if avoiding cycles with any item on path
    not(avoidingCycles(Path1)), not(avoidingCycles(Path2)).

unitUnique(BaseItem, Target1, Path1, Card1, Var1, Target2, Path2, Card2, Var2, HopThreshMin, HopThreshMax) :-

	(
		% first entity case - singleton path + singleton path (ESS)
		entity(BaseItem), BaseEntity = BaseItem,
		Target1 = BaseEntity, singletonPathVar(Var1, Target1), Card1 = one, Path1 = [Target1],
		Target2 = BaseEntity, singletonPathVar(Var2, Target2), Card2 = one, Path2 = [Target2],
		Var1 \== Var2;

		% second entity case - entity path + singleton path (EES)
		entity(BaseItem), BaseEntity = BaseItem,
		entityPathVar(BaseEntity, Target1, Var1, Card1, Path1, HopThreshMax),
		length(Path1, X), X > HopThreshMin,
		Target2 = BaseEntity, singletonPathVar(Var2, Target2), Card2 = one, Path2 = [Target2],
		Var1 \== Var2;

		% third entity case - relationship path + singleton path (ERS)
		entity(BaseItem), BaseEntity = BaseItem,
		relationshipPathVar(BaseEntity, Target1, Var1, Card1, Path1, HopThreshMax),
		length(Path1, X), X > HopThreshMin,
		Target2 = BaseEntity, singletonPathVar(Var2, Target2), Card2 = one, Path2 = [Target2],
		Var1 \== Var2;

		% first relationship case - singleton path + singleton path (RSS)
		relationship(BaseItem), BaseRelationship = BaseItem,			
		Target1 = BaseRelationship, singletonPathRelationshipVar(Var1, Target1), Card1 = one, Path1 = [Target1],
			% relationship existence is trivially a precondition of its attributes
			Var1 \== Target1,
		Target2 = BaseRelationship, singletonPathRelationshipVar(Var2, Target2), Card2 = one, Path2 = [Target2],
			% relationship attributes cannot cause its existence
			Var2 \== Target2,
		Var1 \== Var2;

		% second relationship case - entity path + singleton path (RES)
		relationship(BaseItem), BaseRelationship = BaseItem,
		entityPathRelationshipVar(BaseRelationship, Target1, Var1, Card1, Path1, HopThreshMax),
		length(Path1, X), X > HopThreshMin,
		Target2 = BaseRelationship, singletonPathRelationshipVar(Var2, Target2), Card2 = one, Path2 = [Target2],
		( % If outcome is relationship existence, then it cannot appear in the treatment path (except for the base)
			Var2 \== Target2;
			Var2 == Target2, numOccurrences(Target2, Path1, R), R =< 1
		),		
		Var1 \== Var2;

		% third relationship case - relationship path + singleton path (RRS)
		relationship(BaseItem), BaseRelationship = BaseItem,
		relationshipPathRelationshipVar(BaseRelationship, Target1, Var1, Card1, Path1, HopThreshMax),
		length(Path1, X), X > HopThreshMin,
		Target2 = BaseRelationship, singletonPathRelationshipVar(Var2, Target2), Card2 = one, Path2 = [Target2],
		( % If outcome is relationship existence, then it cannot appear in the treatment path (except for the base)
			Var2 \== Target2;
			Var2 == Target2, numOccurrences(Target2, Path1, R), R =< 1
		),		
		Var1 \== Var2
	),

    % Additional constraints
    
    % Check if avoiding cycles with any item on path
    not(avoidingCycles(Path1)).
    
    
printResultsFile(FILENAME, L, HopThresh1, HopThresh2) :-
	open(FILENAME, write, OUT),
	findall((BaseItem, Target1, Path1, Card1, Var1, Target2, Path2, Card2, Var2, HopThresh1, HopThresh2),
	unitAll(BaseItem, Target1, Path1, Card1, Var1, Target2, Path2, Card2, Var2, HopThresh1, HopThresh2), L),
	foreachl(member(X, L), (write(OUT, X), write(OUT, '\n'))),
	close(OUT).
        
printResults(L, HopThresh1, HopThresh2) :-
	findall((BaseItem, Target1, Path1, Card1, Var1, Target2, Path2, Card2, Var2, HopThresh1, HopThresh2),
	unitAll(BaseItem, Target1, Path1, Card1, Var1, Target2, Path2, Card2, Var2, HopThresh1, HopThresh2), L),
	foreachl(member(X, L), (write(X), nl)).

getNumUnits(Y, HopThresh1, HopThresh2) :-
	findall((BaseItem, Target1, Path1, Card1, Var1, Target2, Path2, Card2, Var2, HopThresh1, HopThresh2),
	unitAll(BaseItem, Target1, Path1, Card1, Var1, Target2, Path2, Card2, Var2, HopThresh1, HopThresh2), L),
	length(L, Y).
        
getNumUnitsUnique(Y, HopThreshMin, HopThreshMax) :-
	findall((BaseItem, Target1, Path1, Card1, Var1, Target2, Path2, Card2, Var2),
	unitUnique(BaseItem, Target1, Path1, Card1, Var1, Target2, Path2, Card2, Var2, HopThreshMin, HopThreshMax), L),
	length(L, Y).

getRandomUnit(BaseItem, Target1, Path1, Card1, Var1, Target2, Path2, Card2, Var2, HopThreshMin, HopThreshMax) :-
    findall((BaseItem, Target1, Path1, Card1, Var1, Target2, Path2, Card2, Var2),
            unitUnique(BaseItem, Target1, Path1, Card1, Var1, Target2, Path2, Card2, Var2, HopThreshMin, HopThreshMax), L),
    length(L, Y), X is random(Y), nth0(X, L, (BaseItem, Target1, Path1, Card1, Var1, Target2, Path2, Card2, Var2)).