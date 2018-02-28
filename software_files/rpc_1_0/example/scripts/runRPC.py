#Part of the open-source KDL Relational PC package
# (see LICENSE for copyright and license information).
###########IMPORT MODULES###########
from rpc.datagen import DataGenerator
from rpc.datagen import CausalModelGenerator
from rpc.dataretrieval import Database
from rpc.model.util import ModelSupport
from rpc.model import RelationalPC
from rpc.model.scoring import ModelScoring
####################################

###########SET KEY PARAMETERS##########
dbName = ckd.args[0]
schemaName = ckd.args[1]

ckd.loadRPC()
ckd.loadSchema(schemaName)

hopThreshold = 2
maxParents = 2
rpcDepth = 2
numDependencies = 10
sampleSize = 800
########################################

##########GENERATE DATA##########
#Generate a random (known) causal model structure
cg = CausalModelGenerator(numDependencies, hopThreshold, maxParents)
trueModel = cg.getModel()		
#trueModel.getDotFile("trueModel_dep" + str(numDependencies) + ".dot", 1)

#Parameterize the dependencies in the causal model
p = cg.parameterize({"ab":2.0, "da":1.0, "bc":2.0})
	
#Generate data from known causal model structure and parameters
dg = DataGenerator(dbName)
dg.generate(cg, p, {"a":sampleSize, "b":2*sampleSize, "c":sampleSize/2, "d":sampleSize})
dg.close()
##################################

##########SETUP FOR RELATIONAL PC##########
Database.open(dbName)
ckd.setHopThreshold(hopThreshold)

#Retrieve units
allUnits = ckd.getUnits()
uniqueUnits = ckd.getUniqueUnits()

#Initialize model support data structure
ckd.modelSupport = ModelSupport(allUnits, uniqueUnits, hopThreshold)
###########################################

##########RUN RELATIONAL PC ALGORITHM##########
rpc = RelationalPC(ckd.modelSupport)
rpc.setSignficanceThresholdAdjust(0.01)
rpc.setStrengthOfEffectThreshold(0.1)

#Phase I
rpc.identifySkeleton(rpcDepth)

#Score the model
learnedModel = rpc.getModel()
#learnedModel.getDotFile("learnedModelPhaseI_dep" + str(numDependencies) + "_ss" + str(sampleSize) + ".dot", 1)
errorCts = ModelScoring.getErrorCounts(trueModel, learnedModel)
trivialOrient = list(errorCts)[0]

#Phase II
rpc.orientEdges()

#Print out resulting constraints and dependencies
print "Independence constraints:"
for constraint in rpc.getConstraints():
	print "\t" + constraint

print "Dependencies:"
for dependence in rpc.getDependencies():
	print "\t" + str(dependence)
#################################################################################
			
##########SCORE THE MODEL######################################################
learnedModel = rpc.getModel()
#learnedModel.getDotFile("learnedModelPhaseII_dep" + str(numDependencies) + "_ss" + str(sampleSize) + ".dot", 1)
errorCts = ModelScoring.getErrorCounts(trueModel, learnedModel)

sprecision = ModelScoring.getSPrecision(errorCts)
srecall = ModelScoring.getSRecall(errorCts)
cprecision = ModelScoring.getCPrecision(errorCts)
crecall = ModelScoring.getCRecall(errorCts)

ruleFreqs = ModelScoring.getEdgeRuleFrequencies()
cd = ruleFreqs.get("CD") if ruleFreqs.containsKey("CD") else 0
rem = ruleFreqs.get("REM") if ruleFreqs.containsKey("REM") else 0
knc = ruleFreqs.get("KNC") if ruleFreqs.containsKey("KNC") else 0
ca = ruleFreqs.get("CA") if ruleFreqs.containsKey("CA") else 0

oracleCRecall = ModelScoring.getOracleCRecall(ckd.modelSupport, trueModel)

print "SPrecision:", sprecision
print "SRecall:", srecall
print "CPrecision:", cprecision
print "CRecall:", crecall
print "Triv:", trivialOrient
print "Edge Rule Frequencies:", "cd:", cd, "rem:", rem, "knc:", knc, "ca:", ca
print "Oracle CRecall:", oracleCRecall

#Close the database connection
Database.close()
#################################################################################
