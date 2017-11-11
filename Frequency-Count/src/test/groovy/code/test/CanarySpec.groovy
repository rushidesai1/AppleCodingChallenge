package code.test

import spock.lang.Specification

/**
 * Created by Rushi Desai on 11/9/2017
 *
 * Just a dummy test class
 */
class CanarySpec extends Specification {

    def "Canary Test to check env is working alright"() {
        expect:
        true
    }
}
