class Views extends Delegator {
    Views(def view) {
        super(view)
    }

    def defaultColumns() {
        columns {
            status()
            weather()
            name()
            lastSuccess()
            lastFailure()
            lastDuration()
            buildButton()
        }
    }

    def rxFilter(String filter) {
        jobs {
            regex(filter)
        }
    }

    static def basic(def that, String name, String rxFilter) {
        def view = new Views(that.listView(name))
        view.defaultColumns()
        view.recurse()
        view.rxFilter(rxFilter)
        view
    }

}
