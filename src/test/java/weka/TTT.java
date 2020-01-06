package weka;

/**
 * @ClassName TTT
 * @Description
 * @Author 林春永
 * @Date 2020/1/5
 * @Version 1.0
 **/
public class TTT {
    public static void main(String[] args) throws Exception {
        String [] options=new String[]{"-B","6"};
       System.out.println(getOption("B", options));
    }


    public static int getOptionPos(String flag, String[] options) {
        if(options == null) {
            return -1;
        } else {
            for(int i = 0; i < options.length; ++i) {
                if(options[i].length() > 0 && options[i].charAt(0) == 45) {
                    try {
                        Double.valueOf(options[i]);
                    } catch (NumberFormatException var4) {
                        if(options[i].equals("-" + flag)) {
                            return i;
                        }

                        if(options[i].charAt(1) == 45) {
                            return -1;
                        }
                    }
                }
            }

            return -1;
        }
    }

    public static String getOption(String flag, String[] options) throws Exception {
        int i = getOptionPos(flag, options);
        if(i > -1) {
            if(options[i].equals("-" + flag)) {
                if(i + 1 == options.length) {
                    throw new Exception("No value given for -" + flag + " option.");
                }

                options[i] = "";
                String newString = new String(options[i + 1]);
                options[i + 1] = "";
                return newString;
            }

            if(options[i].charAt(1) == 45) {
                return "";
            }
        }

        return "";
    }
}
