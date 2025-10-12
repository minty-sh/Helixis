package sh.minty.helixis.commands;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.Hashtable;
import java.util.concurrent.Callable;

@Command(
    name = "dns",
    mixinStandardHelpOptions = true,
    description = "DNS lookup utilities.",
    subcommands = {
        DnsCommand.LookupCommand.class
    }
)
public class DnsCommand {

    @Command(name = "lookup", mixinStandardHelpOptions = true, description = "Performs a DNS lookup for a given hostname.")
    static class LookupCommand implements Callable<Integer> {
        @Parameters(index = "0", description = "The hostname to lookup.")
        private String domainName;

        @Option(names = {"-t", "--type"}, description = "The record type to lookup (A, AAAA, MX, TXT, CNAME, NS). Defaults to A.", defaultValue = "A")
        private String recordType;

        @Override
        public Integer call() {
            try {
                Hashtable<String, String> env = new Hashtable<>();
                env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");
                DirContext context = new InitialDirContext(env);
                Attributes attributes = context.getAttributes(domainName, new String[]{recordType});
                Attribute attribute = attributes.get(recordType);

                if (attribute == null) {
                    System.out.println("No " + recordType + " records found for " + domainName);
                    return 0;
                }

                NamingEnumeration<?> all = attribute.getAll();
                while (all.hasMore()) {
                    System.out.println(all.next());
                }
                return 0;
            } catch (Exception e) {
                System.err.println("Error performing DNS lookup: " + e.getMessage());
                return 1;
            }
        }
    }
}
