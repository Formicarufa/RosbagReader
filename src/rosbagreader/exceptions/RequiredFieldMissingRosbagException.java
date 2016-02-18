/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rosbagreader.exceptions;

/**
 * This exception is thrown when there should be some header field present
 * but it is not.
 * @author Tomas Prochazka
 */
public class RequiredFieldMissingRosbagException extends RosbagException {

    public RequiredFieldMissingRosbagException(String message) {
        super(message);
    }

    public RequiredFieldMissingRosbagException() {
    }

}
