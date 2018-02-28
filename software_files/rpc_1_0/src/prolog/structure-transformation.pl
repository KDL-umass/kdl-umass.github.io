/*
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * structure-transformation.pl contains the necessary rules to identify
   paths from a database schema.
 */

% Basic path consists of a single entity
singletonPath(BaseEntity) :-
	entity(BaseEntity).

% Basic path consists of a single relationship
singletonPathRelationship(BaseRelationship) :-
	relationship(BaseRelationship).

% Entity paths have a base entity and a targeted entity
entityPath(BaseEntity, TargetEntity, Card, OutPath, HopThresh) :-
    entityPath(BaseEntity, TargetEntity, Card, [BaseEntity], OutPath, HopThresh).

entityPath(BaseEntity, TargetEntity, Card, InPath, OutPath, HopThresh) :-
	% Base Case:
	% Check if the base entity connects to the target entity through a single relationship
	length(InPath, X), X < HopThresh,
	doubleHopFromEntity(BaseEntity, Relationship, TargetEntity, Card),
	append(InPath, [Relationship, TargetEntity], OutPath),
	( 
		isDoublingBack(OutPath), Card == many ;  
		not(isDoublingBack(OutPath))
	);
	
	% Recursive Step:
	% BaseEntity - MiddleEntity and entityPath from MiddleEntity to TargetEntity
	length(InPath, X), X < HopThresh,
	doubleHopFromEntity(BaseEntity, MiddleRelationship, MiddleEntity, Card1),
	append(InPath, [MiddleRelationship, MiddleEntity], InPath1),
	( 
		isDoublingBack(InPath1), Card1 == many ;  
		not(isDoublingBack(InPath1)) 
	),	
	entityPath(MiddleEntity, TargetEntity, Card2, InPath1, OutPath, HopThresh),
	combineCards(Card1, Card2, Card).

% Entity paths for relationships have a base relationship and a targeted entity
entityPathRelationship(BaseRelationship, TargetEntity, Card, OutPath, HopThresh) :-
    entityPathRelationship(BaseRelationship, TargetEntity, Card, [], OutPath, HopThresh).

entityPathRelationship(BaseRelationship, TargetEntity, Card, InPath, OutPath, HopThresh) :-
	% Base Case:
	% Check if the base relationship connects to the target entity through a single hop
	singleHopFromRelationship(BaseRelationship, TargetEntity),
	Card = one,	append(InPath, [BaseRelationship, TargetEntity], OutPath);
	
	% Recursive Step:
	% BaseRelationship - MiddleEntity and *entityPath* from MiddleEntity to TargetEntity
	length(InPath, X), X < HopThresh,
	singleHopFromRelationship(BaseRelationship, MiddleEntity), Card1 = one,
	append(InPath, [BaseRelationship, MiddleEntity], InPath1),
	entityPath(MiddleEntity, TargetEntity, Card2, InPath1, OutPath, HopThresh),

	%check that the path to the first instance of the other connected entity on the base relationship is many
	%IF the entityPath comes back with it on the path
	entity(ConnectedEntity), ConnectedEntity \== MiddleEntity, cardinality(BaseRelationship, ConnectedEntity, _),
	
	(
	    memberchk(ConnectedEntity, OutPath), [_|T] = OutPath, sublisty(T, ConnectedEntity, OutPathSub),
	        entityPath(MiddleEntity, ConnectedEntity, CardConnect, OutPathSub, HopThresh), CardConnect == many;
	    not(memberchk(ConnectedEntity, OutPath))
	 ),
	
	combineCards(Card1, Card2, Card).

% Relationship paths have a base entity and a targeted relationship	
relationshipPath(BaseEntity, TargetRelationship, Card, OutPath, HopThresh) :- 
	relationshipPath(BaseEntity, TargetRelationship, Card, [BaseEntity], OutPath, HopThresh).

relationshipPath(BaseEntity, TargetRelationship, Card, InPath, OutPath, HopThresh) :-
	
	% Base Case:
	% Check if the base entity connects to the target entity through a single relationship
	length(InPath, X), X =< HopThresh,
	singleHopFromEntity(BaseEntity, TargetRelationship, Card),
	append(InPath, [TargetRelationship], OutPath),
    (
        isDoublingBack(OutPath), Card == many;
        not(isDoublingBack(OutPath))
    );
	
	% Recursive Step:
	% Find an intermediate hop
	length(InPath, X), X =< HopThresh,
	singleHopFromEntity(BaseEntity, MiddleRelationship, Card1),
	append(InPath, [MiddleRelationship], InPath1),
	(
		isDoublingBack(InPath1), Card1 == many;
		not(isDoublingBack(InPath1))
	),		
	relationshipPathRelationship(MiddleRelationship, TargetRelationship, Card2, InPath1, OutPath, HopThresh),
	combineCards(Card1, Card2, Card).	
	
% Relationship paths have a base entity and a targeted relationship	
relationshipPathRelationship(BaseRelationship, TargetRelationship, Card, OutPath, HopThresh) :- 
	relationshipPathRelationship(BaseRelationship, TargetRelationship, Card, [BaseRelationship], OutPath, HopThresh).

relationshipPathRelationship(BaseRelationship, TargetRelationship, Card, InPath, OutPath, HopThresh) :-
	
	% Base Case:
	% Check if the base entity connects to the target entity through a single relationship
	length(InPath, X), X < HopThresh,
	doubleHopFromRelationship(BaseRelationship, MiddleEntity, TargetRelationship, Card),
	%Make sure path does not repeat itself (e.g., [BC B BC B] returns to same instances)
	( 
		X >= 2, reverse(InPath, InPathRev), [_|[PreviousEntity|_]] = InPathRev, MiddleEntity \== PreviousEntity;
		X < 2
	),
	append(InPath, [MiddleEntity, TargetRelationship], OutPath),
    (
        isDoublingBack(OutPath), cardinality(BaseRelationship, MiddleEntity, many);
        not(isDoublingBack(OutPath))
    );
	
	% Recursive Step:
	% Find an intermediate hop
	length(InPath, X), X < HopThresh-1,
	doubleHopFromRelationship(BaseRelationship, MiddleEntity, MiddleRelationship, Card1),
	%Make sure path does not repeat itself (e.g., [BC B BC B] returns to same instances)
	( 
		X >= 2, reverse(InPath, InPathRev), [_|[PreviousEntity|_]] = InPathRev, MiddleEntity \== PreviousEntity;
		X < 2
	),	
	append(InPath, [MiddleEntity, MiddleRelationship], InPath1),
	(
		isDoublingBack(InPath1), cardinality(BaseRelationship, MiddleEntity, many);
		not(isDoublingBack(InPath1))
	),			
	relationshipPathRelationship(MiddleRelationship, TargetRelationship, Card2, InPath1, OutPath, HopThresh),
	combineCards(Card1, Card2, Card).		
	
%%%%%Helper functions to define paths%%%%%
% Single hop on the schema Ent - Rel
singleHopFromEntity(BaseEntity, Relationship, Card) :-
	entity(BaseEntity), entity(TargetEntity), BaseEntity \== TargetEntity,
	relationship(Relationship),	
	cardinality(Relationship, BaseEntity, _), 
	cardinality(Relationship, TargetEntity, Card).

% Single hop on the schema Rel - Ent
singleHopFromRelationship(BaseRelationship, TargetEntity) :-
	relationship(BaseRelationship), entity(TargetEntity),
	cardinality(BaseRelationship, TargetEntity, _).

% Two hops on the schema Ent1 - Rel1 - Ent2
doubleHopFromEntity(BaseEntity, Relationship, TargetEntity, Card) :-
	entity(BaseEntity), entity(TargetEntity), BaseEntity \== TargetEntity,
	relationship(Relationship),	
	cardinality(Relationship, BaseEntity, _), 
	cardinality(Relationship, TargetEntity, Card).

% Two hops on the schema Rel1 - Ent - Rel2
doubleHopFromRelationship(BaseRelationship, Entity, TargetRelationship, Card) :-
	relationship(BaseRelationship), entity(Entity), relationship(TargetRelationship), 
	cardinality(BaseRelationship, Entity, _), cardinality(TargetRelationship, Entity, _),
	entity(ConnectedEntity), ConnectedEntity \== Entity,
	%cardinality of the single hop is taken from the connected entity
	cardinality(TargetRelationship, ConnectedEntity, Card),
	%Prohibits returning to the same instance of the relationship
	( 
		BaseRelationship == TargetRelationship, Card == many;
		BaseRelationship \== TargetRelationship
	).


% Combine two cardinalities, the "many" always dominates
combineCards(C1, C2, Card) :-
	% The -> acts as an if ... then statement
	C1 == one, C2 == one -> Card = one; Card = many.

% Get sublist from start to first instance of given item
% e.g. sublist([d, a, c, b, c, a, b, d], b, Out) -> Out = [d, a, c, b]
sublisty(L, Item, OutList) :- sublisty(L, Item, [], OutList).

sublisty(L, Item, InList, OutList) :-
	L == [], OutList = InList, !;
    [H|T] = L, H == Item, append(InList, [H], OutList), !;

    [H|T] = L, H \== Item, append(InList, [H], InList1), sublisty(T, Item, InList1, OutList).
    
    
    
% Checks if final element in list is doubling back
% Assumes lists of [ent, rel, ent, rel, ent, ...]
% Only needs to check the last 5 elements for specific pattern
% e.g., isDoublingBack([a, ab, b, ab, a]) -> true
%		isDoublingBack([a, ab, b, bc, c, bc, b]) -> true
% 		isDoublingBack([a, ab, b, bc, c, bc, b, ab, a]) -> false
%		isDoublingBack([a, ab, b, bc, c, bc, b, ab, a, da, d ,da, a]) -> true
isDoublingBack(L) :- 
	length(L, X), X < 5, fail;
	reverse(L, RevL), [Ent1 | [Rel1 | [_ | [Rel2 | [Ent3 | _]]]]] = RevL, Rel1 == Rel2, Ent1 == Ent3.



