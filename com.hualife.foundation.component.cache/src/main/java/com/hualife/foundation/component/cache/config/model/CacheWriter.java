//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.06.08 at 04:15:27 ���� CST 
//


package com.hualife.foundation.component.cache.config.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.hualife.com/foundation/component/cache}extProperties"/>
 *         &lt;choice>
 *           &lt;element ref="{http://www.hualife.com/foundation/component/cache}asyn"/>
 *           &lt;element ref="{http://www.hualife.com/foundation/component/cache}syn"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *       &lt;attribute name="impl" use="required" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "extProperties",
    "asyn",
    "syn"
})
@XmlRootElement(name = "cacheWriter")
public class CacheWriter {

    @XmlElement(required = true)
    protected ExtProperties extProperties;
    protected Asyn asyn;
    protected Boolean syn;
    @XmlAttribute(required = true)
    @XmlSchemaType(name = "anySimpleType")
    protected String impl;

    /**
     * Gets the value of the extProperties property.
     * 
     * @return
     *     possible object is
     *     {@link ExtProperties }
     *     
     */
    public ExtProperties getExtProperties() {
        return extProperties;
    }

    /**
     * Sets the value of the extProperties property.
     * 
     * @param value
     *     allowed object is
     *     {@link ExtProperties }
     *     
     */
    public void setExtProperties(ExtProperties value) {
        this.extProperties = value;
    }

    /**
     * Gets the value of the asyn property.
     * 
     * @return
     *     possible object is
     *     {@link Asyn }
     *     
     */
    public Asyn getAsyn() {
        return asyn;
    }

    /**
     * Sets the value of the asyn property.
     * 
     * @param value
     *     allowed object is
     *     {@link Asyn }
     *     
     */
    public void setAsyn(Asyn value) {
        this.asyn = value;
    }

    /**
     * Gets the value of the syn property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isSyn() {
        return syn;
    }

    /**
     * Sets the value of the syn property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSyn(Boolean value) {
        this.syn = value;
    }

    /**
     * Gets the value of the impl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getImpl() {
        return impl;
    }

    /**
     * Sets the value of the impl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setImpl(String value) {
        this.impl = value;
    }

}
