package lotto.controller;

import java.util.List;
import java.util.Scanner;
import lotto.domain.LottoAnnouncement;
import lotto.domain.LottoProfitRate;
import lotto.domain.LottoResult;
import lotto.domain.Lottos;
import lotto.domain.Money;
import lotto.domain.Piece;
import lotto.domain.generator.LottoAutoNumberGenerator;
import lotto.domain.generator.LottoManualNumberGenerator;
import lotto.exception.LottoAnnouncementException;
import lotto.exception.LottoException;
import lotto.exception.MoneyException;
import lotto.exception.PieceException;
import lotto.viewer.InputView;
import lotto.viewer.OutputView;

public class LottoStore {

    private final InputView inputView;
    private final OutputView outputView;

    public LottoStore() {
        Scanner scanner = new Scanner(System.in);
        inputView = new InputView(scanner);
        outputView = new OutputView();
    }

    public void process() {
        Lottos lottos = buyLotto();
        LottoAnnouncement lottoAnnouncement = receiveValidLottoAnnouncement();
        LottoResult lottoResult = new LottoResult(lottoAnnouncement, lottos);
        LottoProfitRate lottoProfitRate = lottoResult.getProfitRate();
        outputView.printLottoStatistics(lottoResult, lottoProfitRate);
    }

    public Lottos buyLotto() {
        Money possessedMoney = receiveValidMoney();
        Piece manualPieces = receiveManualPieces(possessedMoney);
        Piece autoPieces = manualPieces.getAnotherPiece(possessedMoney);
        LottoAutoNumberGenerator lottoAutoGenerator = new LottoAutoNumberGenerator();
        Lottos purchasedLottos = boughtManualLottos(manualPieces);
        purchasedLottos.addExtraPieces(lottoAutoGenerator, autoPieces.getPieceNumber());
        outputView.printPurchasedLottos(purchasedLottos, manualPieces);
        return purchasedLottos;
    }

    private Money receiveValidMoney() {
        try {
            return inputView.purchaseMoney();
        } catch (MoneyException moneyException) {
            outputView.printLottoException(moneyException);
            return receiveValidMoney();
        }
    }

    private LottoAnnouncement receiveValidLottoAnnouncement() {
        List<Integer> winnerNumbers;
        int bonusNumber;
        try {
            winnerNumbers = inputView.inputWinnerNumbers();
            bonusNumber = inputView.inputBonusNumber();
            return new LottoAnnouncement(winnerNumbers, bonusNumber);
        } catch (LottoAnnouncementException lottoAnnouncementException) {
            outputView.printLottoException(lottoAnnouncementException);
            return receiveValidLottoAnnouncement();
        }
    }

    private Piece receiveManualPieces(Money possessedMoney) {
        try {
            return inputView.inputManualPieces(possessedMoney);
        } catch (PieceException pieceException) {
            outputView.printLottoException(pieceException);
            return inputView.inputManualPieces(possessedMoney);
        }
    }

    private Lottos boughtManualLottos(Piece manualPiece) {
        LottoManualNumberGenerator lottoManualGenerator;
        try {
            List<List<Integer>> manualNumbers = inputView.receiveManualNumbers(manualPiece);
            lottoManualGenerator = new LottoManualNumberGenerator(manualNumbers);
            return new Lottos(lottoManualGenerator, manualPiece.getPieceNumber());
        } catch (LottoException lottoException) {
            outputView.printLottoException(lottoException);
            return boughtManualLottos(manualPiece);
        }
    }
}
