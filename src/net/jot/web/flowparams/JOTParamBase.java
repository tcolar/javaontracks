/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.web.flowparams;

/**
* Object reprensenting the parameters of a flow Directive.
@author thibautc
*/
public class JOTParamBase
{

  public static final int FLOW_PARAM_NONE = 0;
  public static final int FLOW_PARAM_CALL = 1;
  public static final int fLOW_PARAM_RETURN_TO_MARKER = 2;
  public static final int FLOW_PARAM_REDIRECT_TO = 3;
  public static final int FLOW_PARAM_RENDER_PAGE = 4;
  public static final int FLOW_PARAM_RENDER_STATIC_PAGE = 5;
  public static final int FLOW_PARAM_SET_MARKER = 6;
  //public static final int FLOW_PARAM_FORWARD_TO=7;

  public static final int FLOW_PARAM_PROCESS_FORM = 8;
  public static final int FLOW_PARAM_CONTINUE_TO = 9;
  private int lineNumber = 0;
  String ifResultValue = null;
  boolean ifResultCheckIfTrue = true;
  String[] params ={};

  int type=FLOW_PARAM_NONE;
	
  public JOTParamBase(int lineNumber, int type, String param1)
  {
    this.lineNumber = lineNumber;
    this.type = type;
    params = new String[1];
    params[0] = param1;
  }

  public JOTParamBase(int lineNumber, int type, String param1, String param2)
  {
    this.lineNumber = lineNumber;
    this.type = type;
    params = new String[2];
    params[0] = param1;
    params[1] = param2;
  }

  public JOTParamBase(int lineNumber, int type, String[] params)
  {
    this.lineNumber = lineNumber;
    this.type = type;
    this.params = params;
  }

  public int getLineNumber()
  {
    return lineNumber;
  }

  public void setIfResult(String value, boolean checkIfTrue)
  {
    if (value != null)
    {
      ifResultValue = value;
      ifResultCheckIfTrue = checkIfTrue;
    }
  }

  public void validate() throws Exception
  {

  }

  public boolean isIfResultCheckIfTrue()
  {
    return ifResultCheckIfTrue;
  }

  public String getIfResultValue()
  {
    return ifResultValue;
  }

  public String[] getParams()
  {
    return params;
  }

  public int getType()
  {
    return type;
  }
}
