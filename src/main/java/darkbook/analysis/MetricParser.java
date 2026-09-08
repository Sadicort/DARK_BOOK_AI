package darkbook.analysis;

public class MetricParser {

    public static long parse(String value){

        if(value == null || value.isBlank())
            return 0;

        value = value.replace(",", "")
                .trim()
                .toUpperCase();

        try{

            if(value.endsWith("K")){

                return (long)(Double.parseDouble(
                        value.replace("K","")) * 1000);

            }

            if(value.endsWith("M")){

                return (long)(Double.parseDouble(
                        value.replace("M","")) * 1000000);

            }

            if(value.endsWith("B")){

                return (long)(Double.parseDouble(
                        value.replace("B","")) * 1000000000);

            }

            return Long.parseLong(value);

        }catch(Exception e){

            return 0;

        }

    }

}