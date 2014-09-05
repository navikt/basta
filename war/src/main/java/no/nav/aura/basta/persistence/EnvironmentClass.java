package no.nav.aura.basta.persistence;

public enum EnvironmentClass {




    p("a","c"),
    q("b"),
    t("d"),
    u("e");
    private final String[] hostNamePrefixes;

    private EnvironmentClass(String... hostNamePrefixes){
        this.hostNamePrefixes = hostNamePrefixes;
    }

    public static EnvironmentClass fromHostname(String hostName){
        for (EnvironmentClass environmentClass : values()) {
            for (String hostnamePrefix : environmentClass.hostNamePrefixes) {
                if (hostName.toLowerCase().trim().startsWith(hostnamePrefix)){
                    return environmentClass;
                }
            }
        }
        //Unknown hostname, assume the strongest authentication
        return p;
    }
}