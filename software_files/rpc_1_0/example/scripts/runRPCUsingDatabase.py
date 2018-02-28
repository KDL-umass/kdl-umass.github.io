#Part of the open-source KDL Relational PC package
#(see LICENSE for copyright and license information).
from rpc.dataretrieval import Database
from rpc.model.util import ModelSupport
from rpc.model import RelationalPC

#Set parameters
rpcDepth = 2
hop_threshold = 2

#Setup database and schema
#pass in database name
Database.open(ckd.args[0]) 
ckd.loadRPC()
#pass in schema file
ckd.loadSchema(ckd.args[1]) 

ckd.setHopThreshold(hop_threshold)

#Retrieve units
allUnits = ckd.getUnits()
uniqueUnits = ckd.getUniqueUnits()

#Initialize model support data structure
ckd.modelSupport = ModelSupport(allUnits, uniqueUnits, ckd.getHopThreshold())

##########RUN RELATIONAL PC ALGORITHM#############################################
rpc = RelationalPC(ckd.modelSupport)
rpc.setSignficanceThresholdAdjust(0.01)
rpc.setStrengthOfEffectThreshold(0.1)

#Phase I
rpc.identifySkeleton(rpcDepth)

learnedModel = rpc.getModel()
learnedModel.getDotFile("PhaseI.dot", 1)

#Phase II
rpc.orientEdges()

learnedModel = rpc.getModel()
learnedModel.getDotFile("PhaseII.dot", 1)
			
#Print out resulting constraints and dependencies
print "Independence constraints:"
for constraint in rpc.getConstraints():
	print "\t" + constraint

print "Dependencies:"
for dependence in rpc.getDependencies():
	print "\t" + str(dependence)

#Close the database connection
Database.close()
#################################################################################
