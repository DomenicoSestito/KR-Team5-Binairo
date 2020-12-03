% BINAIRO

int: size;
set of int: dom = 0..1;
% bianco = 1, nero = 0
set of int: indices = 1..size;

array[indices,indices] of opt dom: given;
array[indices,indices] of var dom: matrix;

% assert size pari
constraint assert( size mod 2 == 0, "ERRORE: LA SIZE DEVE ESSERE PARI");

% assert size >= 6, vincolo sopra?
constraint assert( size >= 6, "ERRORE: LA SIZE DEVE ESSERE MAGGIORE DI 6");

% assert dati given in dom lo fa in automatico

%ASSIGNMENT
constraint forall(i in indices, j in indices) ( occurs(given[i,j]) -> matrix[i,j] = given[i,j]);

% CONSTRAINT ORIZZONTALE
constraint forall(i in indices, j in 2..size-1) ( not ( matrix[i,j] = matrix[i,j+1] /\ matrix[i,j] = matrix[i,j-1])); 

% CONSTRAINT VERTICALE
constraint forall(i in 2..size-1, j in indices) ( not ( matrix[i,j] = matrix[i+1,j] /\ matrix[i,j] = matrix[i-1,j])); 

% CONSTRAINT PER ROW NUM BIANCHI = NUM NERI
constraint forall(i in indices) ( sum(j in indices)(matrix[i,j] == 0) == sum(j in indices)(matrix[i,j] == 1) );

% CONSTRAINT PER COL NUM BIANCHI = NUM NERI = size/2
constraint forall(j in indices) ( sum(i in indices)(matrix[i,j] == 0) == sum(i in indices)(matrix[i,j] == 1) );

% CONSTRAINT UNIQUE ROW
constraint forall(i1 in indices, i2 in indices where i1<i2) ( sum(j in indices) (matrix[i1,j] == matrix[i2,j]) != size);

% CONSTRAINT UNIQUE COL
constraint forall(j1 in indices, j2 in indices where j1<j2) ( sum(i in indices) (matrix[i,j1] == matrix[i,j2]) != size);

solve satisfy;

output[
  show_int(3,matrix[i,j]) ++ if j==size then "\n" else " " endif
  | i in indices, j in indices
];