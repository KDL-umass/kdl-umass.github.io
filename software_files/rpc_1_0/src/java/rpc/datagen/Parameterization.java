/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Mar 17, 2010
 * Time: 1:45:27 PM
 */
package rpc.datagen;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

/**
 * Parameterization of a causal model structure.  Contains conditional probability tables for attributes
 * and connector objects and link means for structure (i.e., relationships).
 */
public class Parameterization {

    private Map<String, CPT> parameters; //for attributes
    private Map<String, Connector> connectors; //for structure with no parents
    private Map<String, Map<List<Object>, Double>> linkMeans; //for structure with parents

    /**
     * Initialize a new paramterization object.
     */
    public Parameterization() {
        this.parameters = new HashMap<String, CPT>();
        this.connectors = new HashMap<String, Connector>();
        this.linkMeans = new HashMap<String, Map<List<Object>, Double>>();
    }

    /**
     * Get the CPT associated with a particular attribute.
     * @param vd the vertex data object for the attribute.
     * @return the CPT associated with the attribute.
     */
    public CPT getCPT(VertexData vd) {
        return this.parameters.get(vd.getAttribute());        
    }

    /**
     * Get the connector object associated with a particular structural variable.
     * @param vd the vertex data object for the structural variable.
     * @return the connector object associated with the structural variable.
     */
    public Connector getConnector(VertexData vd) {
        return this.connectors.get(vd.getAttribute());
    }

    /**
     * Get the link means for each configuration of parent values associated with a particular structural variable.
     * @param vd the vertex data object for the structural variable.
     * @return a map from parent value configurations to link means associated with the structural variable.  These
     * will be used as the means for a Poisson connector for the corresponding relationship.
     */
    public Map<List<Object>, Double> getLinkMeans(VertexData vd) {
        return this.linkMeans.get(vd.getAttribute());
    }

    /**
     * Set the CPT for a particular attribute.
     * @param vd the vertex data object for the attribute.
     * @param cpt the CPT associated with the attribute.
     */
    public void setCPT(VertexData vd, CPT cpt) {
        this.parameters.put(vd.getAttribute(), cpt);
    }

    /**
     * Set the connector object for a particular structural variable.
     * @param vd the vertex data object for the structural variable.
     * @param conn the connector object associated with the structural variable.
     */
    public void setConnector(VertexData vd, Connector conn) {
        this.connectors.put(vd.getAttribute(), conn);
    }

    /**
     * Set the link means for each configuration of parent values for a particular structural variable.
     * @param vd the vertex data object for the structural variable.
     * @param parentLinkMeans a map from parent value configurations to link means associated with the
     * structural variable.
     */
    public void setLinkMean(VertexData vd, Map<List<Object>, Double> parentLinkMeans) {
        this.linkMeans.put(vd.getAttribute(), parentLinkMeans);        
    }

    /**
     * Returns a string representation of the parameterization.
     * @return the string representation.
     */
    public String toString() {
        String ret = "";
        ret += "Attribute parameters: " + this.parameters;
        ret += "\nStructure (no parents) connectors: " + this.connectors;
        ret += "\nStructure (parents) link means: " + this.linkMeans;
        return ret;
    }
}