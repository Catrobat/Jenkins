import groovy.transform.Canonical
import hudson.markup.RawHtmlMarkupFormatter
import hudson.plugins.android_emulator.AndroidEmulator
import hudson.security.csrf.DefaultCrumbIssuer
import org.jenkinsci.plugins.ghprb.GhprbTrigger
import org.kohsuke.github.GHCommitState;

@Canonical
class Config {
    String name
    def condition
    def action

    def adapt(def jenkins) {
        if (condition(jenkins)) {
            action(jenkins)
            true
        } else {
            false
        }
    }
}

class Setup {
    List configs = []
    def j = Jenkins.instance

    def config(String name, def condition, def action) {
        configs << new Config(name, condition, action)
    }

    def config(def cfg) {
        configs << cfg
    }

    def run(def closure) {
        closure = closure.clone()
        closure.delegate = this
        closure.resolveStrategy = Closure.DELEGATE_ONLY
        closure()

        run()
    }

    def run() {
        def result = [:].withDefault{ [] }
        def save = false

        for (cfg in configs) {
            if (j.isQuietingDown()) {
                result.skipped << cfg.name
            } else if (cfg.adapt(j)) {
                result.changed << cfg.name
                save = true
            } else {
                result.unmodified << cfg.name
            }
        }

        if (save) {
            j.save()
        }

        result
    }
}

class BaseDescriptorConfig {
    boolean changed
    def desc

    def adapt(def jenkins) {
        changed = false
        desc = jenkins.getDescriptorByType(desc_class)

        doAdapt()

        if (changed) {
            desc.save()
        }

        changed
    }

    Object get(String name) {
        desc."$name"
    }

    void set(String name, Object value) {
        if (desc."$name" != value) {
            desc."$name" = value
            changed = true
        }
    }
}

class AndroidEmulatorConfig extends BaseDescriptorConfig {
    def name = 'Global Android Emulator Settings'
    def desc_class = AndroidEmulator.DescriptorImpl.class

    def doAdapt() {
        shouldKeepInWorkspace = true
        shouldInstallSdk = true
        androidHome = ''
    }
}

class GitHubPullRequestBuilderConfig extends BaseDescriptorConfig {
    def name = 'Global ghprb Config'
    def desc_class = GhprbTrigger.DescriptorImpl.class

    def doAdapt() {
        manageWebhooks = false
        useComments = false
        useDetailedComments = false
        adminlist = ['thmq', 'Bioxar', '84n4n4'].join('\n')
        unstableAs = GHCommitState.FAILURE
        autoCloseFailedPullRequests = false
        displayBuildErrorsOnDownstreamBuilds = false
        requestForTestingPhrase = 'Can a catrobat member please verify this patch? If it doesn\'t ' +
                                  'look evil comment with "test this please" to start the testrun.'
        whitelistPhrase = ''
        okToTestPhrase = /.*ok\W+to\W+test.*/
        retestPhrase = /.*test\W+this\W+please.*/
        skipBuildPhrase = /.*\[skip\W+ci\].*/
        cron = 'H/2 * * * *'
        blackListCommitAuthor = ''
        blackListLabels = ''
        whiteListLabels = ''
        // TODO Build Status Messages
    }
}

print(new Setup().run{
    config('CSRF Protection',
           { !it.crumbIssuer },
           { it.crumbIssuer = new DefaultCrumbIssuer(true) })
    config('Not Sending Usage Statistics',
           { it.isUsageStatisticsCollected() },
           { it.setNoUsageStatistics(true) })
    config('Enable HTML Job/View Descriptions',
           { !(it.markupFormatter instanceof RawHtmlMarkupFormatter) },
           { it.markupFormatter = new RawHtmlMarkupFormatter(false) })
    config(new AndroidEmulatorConfig())
    config(new GitHubPullRequestBuilderConfig())
})
