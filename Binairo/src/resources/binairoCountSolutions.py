from minizinc import Instance, Model, Solver
import os


def main():
    f = open("bin/resources/in","r")
    fout = open("bin/resources/outSolutions", "w")
    
    size = int(f.readline())

    given = []
    for _ in range(size):
        given.append([int(x) if x != 'N' else None for x in f.readline().split()])

    f.close()
    
    model = Model("src/resources/minizinc/Binairo.mzn")
    chuffed = Solver.lookup("chuffed")
    instance = Instance(chuffed, model)
    instance['size'] = size;
    instance['given'] = given

    result = instance.solve(all_solutions=True)
    fout.write(str(len(result)))
    fout.close()
    
if __name__ == '__main__':
    main()