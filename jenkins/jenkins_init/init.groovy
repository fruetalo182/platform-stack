import jenkins.model.*
import hudson.security.*
import jenkins.install.*
import java.util.logging.Logger

// Logger for detailed output in Jenkins logs
def logger = Logger.getLogger("init.groovy.d")

// Get the Jenkins instance
def instance = Jenkins.getInstance()

// Get admin credentials from environment variables
def adminUsername = System.getenv('JENKINS_ADMIN_ID')
def adminPassword = System.getenv('JENKINS_ADMIN_PASSWORD')

// Validate that the environment variables are set
if (!adminUsername || !adminPassword) {
    logger.severe("Environment variables JENKINS_ADMIN_ID and JENKINS_ADMIN_PASSWORD must be set.")
    throw new IllegalStateException("Missing required environment variables for admin user creation.")
}

logger.info("Creating admin user: ${adminUsername}")
def hudsonRealm = new HudsonPrivateSecurityRealm(false)
hudsonRealm.createAccount(adminUsername, adminPassword)
instance.setSecurityRealm(hudsonRealm)

// Set authorization strategy
def strategy = new FullControlOnceLoggedInAuthorizationStrategy()
strategy.setAllowAnonymousRead(false)
instance.setAuthorizationStrategy(strategy)

// Disable setup wizard
instance.setInstallState(InstallState.INITIAL_SETUP_COMPLETED)

// Save initial configuration
instance.save()

// Install plugins
def pm = instance.getPluginManager()
def uc = instance.getUpdateCenter()
uc.updateAllSites()

def pluginsToInstall = [
    "git",
    "github",
    "docker-workflow",
    "credentials-binding",
    "timestamper"
]

// Check for missing plugins and install them
def installed = false
pluginsToInstall.each { pluginName ->
    if (!pm.getPlugin(pluginName)) {
        logger.info("Installing plugin: ${pluginName}")
        def plugin = uc.getPlugin(pluginName)
        if (plugin) {
            def installFuture = plugin.deploy()
            while (!installFuture.isDone()) {
                sleep(1000)
            }
            installed = true
        } else {
            logger.warning("Plugin ${pluginName} not found in Update Center")
        }
    } else {
        logger.info("Plugin ${pluginName} is already installed.")
    }
}

// If any plugins were installed, restart Jenkins
if (installed) {
    logger.info("New plugins installed. Jenkins requires a restart.")
    instance.safeRestart()
} else {
    logger.info("No new plugins were installed.")
}

// Save again to persist installed plugins
instance.save()
