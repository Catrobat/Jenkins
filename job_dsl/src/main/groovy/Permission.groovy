enum Permission {
    JobRead('hudson.model.Item.Read'),
    JobBuild('hudson.model.Item.Build'),
    JobCancel('hudson.model.Item.Cancel')

    String permission

    Permission(String permission) {
        this.permission = permission
    }
}
