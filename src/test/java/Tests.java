import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class Tests {

    @Test
    public void test01() throws UnsupportedEncodingException {
        String queryString = "a=1&b=2&c=&&d==";
        String encode = URLEncoder.encode(queryString, "UTF-8");
        System.out.println("encode: " + encode);
        String decode = URLDecoder.decode(encode, "UTF-8");
        System.out.println("decode: " + decode);
    }
}
