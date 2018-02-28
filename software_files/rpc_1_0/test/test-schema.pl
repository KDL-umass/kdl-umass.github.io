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
%| (w1) |-----/(wx1)\-----| (x1) |-----/(xy1)\---<| (y1) |>--/(yz1)\---<| (z1) |
%|      |     \     /     | (x2) |     \(xy2)/    |      |   \     /    | (z2) |
% ------       \   /      |      |      \   /      ------     \   /     | (z3) |
%               \ /        ------        \ /                   \ /      | (z4) |
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

% Relationship cardinalities
cardinality(ab, a, one).
cardinality(ab, b, many).
cardinality(bc, b, many).
cardinality(bc, c, many).
cardinality(da, d, one).
cardinality(da, a, one).

% Primary keys (defaults to itemName_id)
primaryKey(a_id, a).
primaryKey(c_id, c).
primaryKey(ab_id, ab).
primaryKey(bc_id, bc).

% Foreign keys (defaults to entityName_id)
foreignKey(a_id, ab, a).
foreignKey(b_id, ab, b).

% Attributes
attr(w1, d).
attr(wx1, da).
attr(x1, a).
attr(x2, a).
attr(xy1, ab).
attr(xy2, ab).
attr(y1, b).
attr(yz1, bc).
attr(z1, c).
attr(z2, c).
attr(z3, c).
attr(z4, c).