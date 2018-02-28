/*
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * variable-transformation.pl contains the necessary rules to identify
   all aggregated variables (both attributional and structural) on a path.
 */

singletonPathVar(Var, BaseEntity) :- 
	attr(X, BaseEntity), Var = X, singletonPath(BaseEntity).

singletonPathRelationshipVar(Var, BaseRelationship) :- 
	singletonPathRelationship(BaseRelationship),
	(
		attr(X, BaseRelationship), Var = X;
		relationship(BaseRelationship), Var = BaseRelationship
	).

entityPathVar(BaseEntity, TargetEntity, Var, Card, Path, HopThresh) :-
	entityPath(BaseEntity, TargetEntity, Card, Path, HopThresh),
	attr(X, TargetEntity), Var = X.

entityPathRelationshipVar(BaseRelationship, TargetEntity, Var, Card, Path, HopThresh) :-
	entityPathRelationship(BaseRelationship, TargetEntity, Card, Path, HopThresh),
	attr(X, TargetEntity), Var = X.

relationshipPathVar(BaseEntity, TargetRelationship, Var, Card, Path, HopThresh) :-
	relationshipPath(BaseEntity, TargetRelationship, Card, Path, HopThresh),
	(
		attr(X, TargetRelationship), Var = X;
		relationship(TargetRelationship), Var = TargetRelationship
	).
	
relationshipPathRelationshipVar(BaseRelationship, TargetRelationship, Var, Card, Path, HopThresh) :-
	relationshipPathRelationship(BaseRelationship, TargetRelationship, Card, Path, HopThresh),
	(
		attr(X, TargetRelationship), Var = X;
		relationship(TargetRelationship), Var = TargetRelationship
	).