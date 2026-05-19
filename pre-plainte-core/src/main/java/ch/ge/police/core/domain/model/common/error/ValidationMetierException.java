package ch.ge.police.core.domain.model.common.error;



/**
 * Exception métier utilisée pour signaler une violation
 * de règle de validation dans le domaine.
 */
public class ValidationMetierException extends RuntimeException {

  public ValidationMetierException(String message) {
    super(message);
  }
}
