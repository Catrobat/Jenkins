def createListView(String name_, String pattern) {
    listView(name_) {
        jobs {
            regex(pattern)
        }

        columns {
            status()
            weather()
            name()
            lastSuccess()
            lastFailure()
            lastDuration()
            buildButton()
            builtOnColumn()
        }
    }
}
