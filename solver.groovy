import groovy.transform.Field
import groovyjarjarantlr.collections.List
import groovyx.gpars.GParsPool
import org.testng.collections.ListMultiMap
import experiment

@Field def assignment = []
@Field int[] occ
@Field int numberOfVars
@Field def numberOfClauses
@Field ListMultiMap<Integer, List> map = ListMultiMap.create()
@Field def clauses = []
@Field def vars = []
@Field boolean flag = false

def getData() {
    def cnfFile = new File(args[0])
    def lines = cnfFile.readLines()
    for (String l : lines) {
        l = l.trim()
        if (!l.startsWith("c")) {
            if (l.startsWith("p")) {
                def s = l.split(" ")
                numberOfVars = Integer.parseInt(s[2])
                numberOfClauses = Integer.parseInt(s[3])
                assignment = new Integer[numberOfVars + 1]
                assignment[0] = 0
            } else {
                def s = l.split(" ")
                def singleClause = []
                def itr = s.iterator()
                singleClause = itr.collectMany { Integer.parseInt(it) == 0 ? [] : [Integer.parseInt(it)] }
                singleClause = singleClause.unique()
                singleClause = singleClause.sort()
                for (int lit : singleClause) {
                    map.put(lit, singleClause)
                }
                clauses << singleClause
            }
        }
    }
}

getData()
clauses.unique()

(1..numberOfVars).each {
    vars[it - 1] = it
}

def isUnitClauseExists(var) {
    def posLit = []
    def negLit = []
    if (clauses.contains([-var]) && clauses.contains([var])) {
        return "UNSAT"
    } else {
        if (clauses.contains([var])) {
            posLit << [var]
        }
        if (clauses.contains([-var])) {
            negLit << [-var]
        }
//        GParsPool.withPool {
            clauses.each {
                if (it.contains(var)) {
                    def cc = it - [var]
                    if (cc.every {
                        (assignment[Math.abs(it)] != null && (assignment[Math.abs(it)] * it < 0))
                    }) {
                        posLit << it
                    }
                }
                if (it.contains(-var)) {
                    def cc = it - [-var]
                    if (cc.every {
                        (assignment[Math.abs(it)] != null && (assignment[Math.abs(it)] * it < 0))
                    }) {
                        negLit << it
                    }
                }

            }
//        }
    }

    if (posLit.size() > 0 && negLit.size() > 0) {
        return "uhh"
    } else if (posLit.size()) {
        return 1
    } else if (negLit.size()) {
        return -1
    } else return "random"
}

def solve() {
    def max_tries = Integer.parseInt(args[1])
    try {
        (1..max_tries).each {
            Collections.shuffle(vars)
            for (int i = 0; i <= numberOfVars; i++) {
                assignment[i] = null
            }
            for (int var : vars) {
                def lit = isUnitClauseExists(var)
                if (lit == "UNSAT") {
                    println("c unsat")
                    throw new RuntimeException("UNSAT")
                } else if (lit == "uhh") {
                    if (Math.random() > 0.5 ? 1 : 0) {
                        assignment[Math.abs(var)] = Math.random() > 0.5 ? 1 : -1
                    } else {
                        def getMax = getMaxOccurances(var)
                        assignment[Math.abs(var)] = getMax * -1 //Racing towards unit clauses
                    }
                } else if (lit == 1) {
                    assignment[Math.abs(var)] = 1
                } else if (lit == -1) {
                    assignment[Math.abs(var)] = -1
                } else if (lit == "random") {
//                    assignment[Math.abs(var)] = Math.random() > 0.5 ? 1 : -1
                    def getMax = getMaxOccurances(var) //Racing towards unit clauses
                    assignment[Math.abs(var)] = getMax * -1
                }
            }
            if (checkSAT()) {
                throw new RuntimeException("SAT")
            }
        }
        if (!flag) {
            println("c unsat2")
            println("s UNSATISFIABLE")
        }
    }
    catch (Exception e) {
        if (e.message.toString() == "SAT") {
            println("s SATISFIABLE")
            displayResult()
        } else {
            flag = true
            println("c " + e)
            println("c unsat3")
            println("s UNSATISFIABLE")
        }
    }
}

getNumberOfoccurances()
solve()

def checkSAT() {
    for (ArrayList clause : clauses) {
        if (clause.every {
            assignment[Math.abs(it)] * it < 0
        }) {
            return 0
        }
    }
    return 1
}

def getMaxOccurances(def var) {
    def a = occ[var]
    def b = occ[var + numberOfVars]
    if (a < b) return -1 else 1
}

def getNumberOfoccurances() {
    def tsize = numberOfVars * 2 + 1
    occ = new int[tsize]
    clauses.each {
        clause ->
            clause.each {
                if (it > 0) occ[it] += 1
                else occ[Math.abs(it) + numberOfVars] += 1
            }
    }
}

def displayResult() {
    flag = true
    print("v ")
    for (int i = 1; i <= numberOfVars; i++) {
        print(assignment[i] * i + " ")
    }
}