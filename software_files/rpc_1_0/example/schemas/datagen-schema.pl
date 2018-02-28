/*
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 */
% Datagen example schema
% We use this schema to test the data generator

% This test case simply sets up the ABCD schema.  Specifically, this script makes the following schema:
%
% ______        / \        ______        / \       ______      / \       ______
%| D    |      /DA \      | A    |      /AB \     | B    |    /BC \     | C    |
%| (w)  |-----/(wx) \-----| (x)  |-----/(xy) \---<| (y)  |>--/(yz) \---<| (z)  |
%|      |     \     /     |      |     \     /    |      |   \     /    |      |
% ------       \   /      |      |      \   /      ------     \   /     |      |
%               \ /        -----         \ /                   \ /      |      |
%																		 ------

% Entities
entity(a).
entity(b).
entity(c).
entity(d).

% Relationships
relationship(ab).
relationship(bc).
relationship(da).

%Relationship cardinalities
cardinality(ab, a, one).
cardinality(ab, b, many).
cardinality(bc, b, many).
cardinality(bc, c, many).
cardinality(da, d, one).
cardinality(da, a, one).

% Attributes
attr(w, d).
attr(wx, da).
attr(x, a).
attr(xy, ab).
attr(y, b).
attr(yz, bc).
attr(z, c).
