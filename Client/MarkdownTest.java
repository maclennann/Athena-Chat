import java.lang.String;

public class MarkdownTest{
	public static void main (String[] args){
		String message = args[0];
		int bold=0;
		int italic=0;
		int x=0;
		char current;
		char previous=' ';
		char next=' ';
		for(x=0;x<message.length();x++){
			current = message.charAt(x);
			if(x>0)	previous = message.charAt(x-1);
			if(x!=message.length()-1) next = message.charAt(x+1);
			
			if(current=='*'){
				if(previous=='\\'){
					//do nothing
					System.out.print(current);
					
				}
				else if(next=='*'){
					if(bold==1){
						bold=0;
						System.out.print("</b>");
					}
					else{
						bold=1;
						System.out.print("<b>");
					}
					x++;
				}
				else{
					if(italic==1){
						italic=0;
						System.out.print("</i>");
					}
					else{
						italic=1;
						System.out.print("<i>");
					}
				}
			}
			else{
				if(current=='\\' && next=='*'){}
				else System.out.print(current);
			}
		}
	}
}