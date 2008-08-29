/*
 * JavaOnTrack/Jotwiki
 * Thibaut Colar.
 * tcolar-jot AT colar  DOT net
 */
package net.jot.web.ajax;

import net.jot.web.*;

/**
 *
 * @author thibautc
 */
public class JOTAjaxHelper
{

    public static String getStandardAjaxJscriptFuncName(JOTAjaxProvider ajaxProvider, int id)
    {
        return "jot_ajax_" + ajaxProvider.getClass().getSimpleName() + "_" + id;
    }

    /**
     * Returns a standrad ajax call piece of javascript(no javascript tag) for an AjaxProvider.
     * see JOTExampleWidget for example 
     * @param ajaxProvider
     * @param id : unique id for this widget on the page
     * @return
     */
    public static String getAjaxJavascript(JOTAjaxProvider ajaxProvider, int id)
    {
        String functionName = ajaxProvider.getJscriptFuncName();
        String jscript = "// AJAX request object \n";
        jscript += "var req_" + id + "; if (typeof XMLHttpRequest != 'undefined') {req_" + id + " = new XMLHttpRequest();} else if (window.ActiveXObject) {req_" + id + " = new ActiveXObject('Microsoft.XMLHTTP');}\n";

        jscript += "\n// AJAX GET CALL\n";
        jscript += "function " + functionName + "() {";
        // TODO: use args ?
        //jscript+=       "var idField = document.getElementById(elemId);";
        jscript += "var url = '" + ajaxProvider.getAjaxAction() + "';";//+"?xxx=' + encodeURIComponent(idField.value);\n";
        jscript += "req_" + id + ".open('GET', url, true);";
        jscript += "req_" + id + ".onreadystatechange = " + ajaxProvider.getJscriptCallbackFunctionName() + ";";
        jscript += "req_" + id + ".send(null);";
        jscript += "}\n";
        
        jscript += "\n// AJAX FORM PUT CALL : formId: ID of the html form element to submit\n";
        jscript += "function " + functionName + "_form(formId) {";
        jscript += "var url = '" + ajaxProvider.getAjaxAction() + "';";
        jscript += "var params='';";
        jscript += "var form = document.getElementById(formId);";
        jscript += "var elems = form.getElementsByTagName('input');";
        jscript += "for(var i=0;i!=elems.length;i++){";
        jscript += "  if(!elems[i].disabled && elems[i].name!=''){";
        jscript += "    params+=escape(encodeURI(elems[i].name))+'='+escape(encodeURI(elems[i].value))+'&';";
        jscript += "  }";
        jscript += "}";
        jscript += "var elems = form.getElementsByTagName('select');";
        jscript += "for(var i=0;i!=elems.length;i++){";
        jscript += "  for(var j=0;j!=elems[i].options.length;j++){";
        jscript += "    if(!elems[i].disabled && elems[i].options[j].selected){params+=encodeURI(elems[i].name)+'='+escape(encodeURI(elems[i].options[j].value))+'&';}";
        jscript += "  }";
        jscript += "}";
        //jscript += "alert(params);";
        jscript += "req_" + id +".open('POST', url, true);";
        jscript += "req_" + id +".onreadystatechange = " + ajaxProvider.getJscriptCallbackFunctionName() +";";
        jscript += "req_" + id +".setRequestHeader('Content-type', 'application/x-www-form-urlencoded');";
        jscript += "req_" + id +".setRequestHeader('Content-length', url.length);";
        jscript += "req_" + id +".setRequestHeader('Connection', 'close');";
        jscript += "req_" + id +".send(params);";
        jscript += "}\n";
        
        return jscript;
    }

    /**
     * Build a standard javascript callback method, checking ajax status etc....
     * Adds your custom callback jscript code in it (warps it)
     * see JOTExampleWidget for example 
     * @param innerJavascript
     * @param id
     * @return
     */
    public static String buildStandardCallbackJscript(String innerJavascript, int id)
    {
        String js = "";
        js += "if (req_" + id + ".readyState == 4) {";
        js += "if (req_" + id + ".status == 200) {";
        js += innerJavascript;
        js += "}}";
        return js;
    }

    /**
     * Return piece of javascript code setting a jscript variable(jscriptVarName) with the value of a variable(ajaxVarName) stored in the ajax 'XML' response
     * see JOTExampleWidget for example 
     * @param varName
     * @param uniqueId
     * @return
     */
    public static String setJscriptVarValueFromAjaxVar(String jscriptVarName, String ajaxVarName, int uniqueId)
    {
        return "var root = req_" + uniqueId + ".responseXML.getElementsByTagName('root').item(0);var " + jscriptVarName + " = root.getElementsByTagName('" + ajaxVarName + "')[0].childNodes[0].nodeValue;";
    }

    /**
     * Return piece of javascript code setting an htmlElement(called htmlElemName) 'innerHTML' to the value of 'jscriptVarName'
     * see JOTExampleWidget for example
     * @param htmlElemName
     * @param uniqueId
     * @param jscriptVarName
     * @return
     */
    public static String setElementInnerValue(String htmlElemName, int uniqueId, String jscriptVarName)
    {
        return "document.getElementById('" + htmlElemName + (uniqueId==-1?"":("_"+ uniqueId)) + "').innerHTML = " + jscriptVarName + ";";
    }
}
