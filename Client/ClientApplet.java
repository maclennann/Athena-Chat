// $Id$
import java.applet.*;
import java.awt.*;
import java.io.*;
import java.net.*;
public class ClientApplet extends Applet
{
public void init() {
//String host = getParameter( "host" );
//int port = Integer.parseInt( getParameter( "port" ) );
setLayout( new BorderLayout() );
add( "Center", new Client( "10.", 7777 ) );
}
}
