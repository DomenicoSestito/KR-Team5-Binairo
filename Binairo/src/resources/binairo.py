from minizinc import Instance, Model, Solver
import os


def main():
    fout = open("C:/prova/outputBinairo", "w")
    fout.write("path:\n")
    fout.write(input())
    fout.close()
    
    f = open("in","r")
    
    print(os.path.abspath(fout.name))
    
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
        for line in solution:
            for x in line:
                fout.write(str(x)+" ")
            fout.write("\n")
    fout.close()
if __name__ == '__main__':
    main()