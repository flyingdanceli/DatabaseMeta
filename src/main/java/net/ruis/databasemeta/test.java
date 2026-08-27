package net.ruis.databasemeta;

public class test {

    public static void main(String[] args) {


        String s2 = "011880171\t011881120\n" +
                "011880172\t011881121\n" +
                "011880173\t011881122\n" +
                "011880174\t011881123\n" +
                "011880175\t011881124\n" +
                "011880176\t011881125\n" +
                "011880177\t011881126\n" +
                "011880178\t011881127\n" +
                "011880179\t011881128\n" +
                "011880180\t011881129\n" +
                "011880181\t011881130\n" +
                "011880182\t011881131\n" +
                "011880183\t011881132\n" +
                "011880184\t011881133\n" +
                "011880185\t011881134\n" +
                "011880186\t011881135\n" +
                "011880187\t011881136\n" +
                "011880188\t011881137\n" +
                "011880189\t011881138\n" +
                "011880190\t011881139\n" +
                "011880191\t011881140\n" +
                "011880192\t011881141\n" +
                "011880193\t011881142\n" +
                "011880194\t011881143\n" +
                "011880195\t011881144\n" +
                "011880196\t011881145\n" +
                "011880197\t011881146\n" +
                "011880198\t011881147\n" +
                "011880216\t011881165\n" +
                "011880217\t011881166\n" +
                "011880218\t011881167\n" +
                "011880219\t011881168\n" +
                "011880220\t011881169\n" +
                "011880221\t011881170\n" +
                "011880222\t011881171\n" +
                "011880223\t011881172\n" +
                "011880224\t011881173\n" +
                "011880225\t011881174\n" +
                "011880226\t011881175\n" +
                "011880227\t011881176\n" +
                "011880228\t011881177\n" +
                "011880229\t011881178\n" +
                "011880230\t011881179\n" +
                "011880231\t011881180\n" +
                "011880232\t011881181\n" +
                "011880233\t011881182\n";

        String s =
                "UPDATE tenant_process_instance\n" +
                        "SET form_data = (replace(form_data::text, '%s', '%s'))::jsonb ,\n" +
                        "    form_abstracts = (replace(form_abstracts::text, '%s', '%s'))::jsonb\n" +
                        "WHERE form_data::text LIKE '%%%s%%';";
        String select = " or form_data::text LIKE '%%%s%%' ";

        String[] split = s2.split("\n");
        System.out.println("SELECT * FROM tenant_process_instance WHERE ");
        for (int i = 0; i < split.length; i++) {
            String[] split1 = split[i].split("\t");
            //System.out.println(String.format("旧:%s,  新:%s", split1[0], split1[1]));
            //System.out.println(String.format(select, split1[0]));
            System.out.println(String.format(s, split1[0], split1[1], split1[0], split1[1] , split1[0]));
        }
    }


}
