package net.lr.tinyhttp.impl;

import static org.osgi.service.component.annotations.ReferencePolicyOption.GREEDY;

import java.io.IOException;

import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import net.lr.tinyhttp.Handler;

@ObjectClassDefinition(name = "Tiny HTTP Server Configuration")
@interface ServerConfig {
    String host() default "localhost";
    int port() default 8080;
    int threads() default 10;
    int keepAliveTimeOut() default 10;
  }

@Designate(ocd = ServerConfig.class)
@Component(immediate = true, name="tinyhttp")
public class ServerComponent {

    private Server server;
    
    public ServerComponent() {
        this.server = new Server();
    }

    @Activate
    public void activate(ServerConfig config) {
        this.server.start(config.host(), config.port(), config.threads(), config.keepAliveTimeOut());
    }
    
    @Deactivate
    public void deactivate() throws IOException {
        this.server.close();
    }
    
    @Reference(policy=ReferencePolicy.DYNAMIC, policyOption=GREEDY)
    public void bindHandler(Handler handler, ServiceReference<Handler> ref) {
        String alias = String.valueOf(ref.getProperty(Handler.KEY_ALIAS));
        server.addHandler(alias, handler);
    }
    
    public void unbindHandler(Handler handler, ServiceReference<Handler> ref) {
        String alias = String.valueOf(ref.getProperty(Handler.KEY_ALIAS));
        server.removeHandler(alias);
    }
}
