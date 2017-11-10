package code.test

import spock.lang.Specification

class CanarySpec extends Specification {

    def "Canary Test to check env is working alright"() {
        expect:
        true
    }
}
