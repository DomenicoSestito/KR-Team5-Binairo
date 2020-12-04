#!/usr/bin/env python3


"""
Example input:

3
0 0 0 0 0 0 0 0 0
0 6 8 4 0 1 0 7 0
0 0 0 0 8 5 0 3 0
0 2 6 8 0 9 0 4 0
0 0 7 0 0 0 9 0 0
0 5 0 1 0 6 3 2 0
0 4 0 6 1 0 0 0 0
0 3 0 2 0 7 6 9 0
0 0 0 0 0 0 0 0 0


Example output:

 5  9  3  7  6  2  8  1  4
 2  6  8  4  3  1  5  7  9
 7  1  4  9  8  5  2  3  6
 3  2  6  8  5  9  1  4  7
 1  8  7  3  2  4  9  6  5
 4  5  9  1  7  6  3  2  8
 9  4  2  6  1  8  7  5  3
 8  3  5  2  4  7  6  9  1
 6  7  1  5  9  3  4  8  2
"""


from minizinc import Instance, Model, Solver


def main():
    f = open("in","r")
    
    size = int(f.readline())

    given = []
    for _ in range(size):
        given.append([int(x) if x != 'N' else None for x in f.readline().split()])

    f.close()
    
    model = Model("minizinc/Binairo.mzn")
    gecode = Solver.lookup("gecode")
    instance = Instance(gecode, model)
    instance['size'] = size;
    instance['given'] = given

    result = instance.solve()
    if not result:
        print('NO SOLUTION!')
    else:
        solution = result['matrix']
        # print(solution)
        
        for line in solution:
            print(' '.join([f'{x:2}' for x in line]))
    

if __name__ == '__main__':
    main()

