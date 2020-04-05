import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class Test {

    public static void main(String args[]) {


        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String test = encoder.encode("maison");
        System.out.println(test);
        System.out.println(
                encoder.matches("maison",
                        "$2a$10$QqlFfH0goBmfR2Tz6/wnEuZYPqFk24t6vqjQW0nBhtApghtRWZNtK"));
    }

}
