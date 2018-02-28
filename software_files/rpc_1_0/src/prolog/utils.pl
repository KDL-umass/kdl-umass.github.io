/*
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * utils.pl contains helper rules used across modules.
 */

foreachl(P, Action) :- P, once(Action)	, false.

%Find number of occurrences of Elem in list L
numOccurrences(Elem, L, X) :-
	length(L,0), X is 0, !;
	
	[H|T] = L, numOccurrences(Elem, T, Xnew), 
	(H == Elem, X is Xnew + 1, !; H \== Elem, X is Xnew, !).

%Returns a list with the last element removed
chopLast(Lin, Lout) :-
    length(Lin, X), X<2, Lout = [], !;
    
    reverse(Lin, Lreverse),
    [_|Ltemp] = Lreverse,
    reverse(Ltemp, Lout).


%Returns a list with the last two elements removed
chopLastTwo(Lin, Lout) :-
    length(Lin, X), X<2, Lout = [], !;
    
    reverse(Lin, Lreverse),
    [_|[_|Ltemp]] = Lreverse,
    reverse(Ltemp, Lout).

pause(X) :-
	X > 0 -> Xnew is X - 1, pause(Xnew); write('ding').

% True if paths X and Y have NO overlap
notOverlap(X, Y) :-
 	(length(X,0);length(Y,0)),!;

 	[Hx|Tx] = X,
 	[Hy|Ty] = Y,
 	Hx == Hy -> false; notOverlap(Tx, Ty).

notStartingOverlap(X, Y) :-
    (length(X,0);length(Y,0);length(X,1);length(Y,1)),!;

    [_|[SecondX|_]] = X,
    [_|[SecondY|_]] = Y,
    SecondX \== SecondY.

overlapBool(X, Y, Bool) :-
 	notOverlap(X, Y) -> Bool is 0; Bool is 1.

hasCycle(L) :-
 	length(L, X), X<1, fail;

 	[H|T] = L,
 	(
 	    numOccurrences(H, T, Y), Y>0, !;
        hasCycle(T)
    ).

%Returns the second to last element in L
secondToLast(L, Item) :-
	reverse(L, Ltemp), [_ | [Item | _]] = Ltemp.

	
standardize(Path1, Path2, NewPath1, NewPath2) :-
	%Assumes that Path1 and Path2 have a common base item (first element)
	%And they do not have a starting overlap of more than the first item
	reverse(Path2, RevPath2), [_|TailPath1] = Path1, append(RevPath2, TailPath1, NewPath1),
	[HeadRevPath2|_] = RevPath2, NewPath2 = [HeadRevPath2].
	
avoidingCycles(Path) :-
 	length(Path, X), X<1, fail;

 	[H|T] = Path,
 	(
 	    numOccurrences(H, T, Y), Y>0, avoidCycle(H), !;
        avoidingCycles(T)
    ).		
	
%printToFile(FILENAME, L) :-
%        open(FILENAME, write, OUT),
%        findall((A, B), newRule(A, B), L),
%        length(L, Z),
%        write(Z),
%        foreachl(member(X, L), (write(OUT, X), write(OUT, '\n'))),
%        close(OUT).