package org.hzero.generator.scan.infra.enums;

/**
 * description
 *
 * @author fanghan.liu 2020/04/16 10:59
 */
public enum CliVersion {

    /**
     * 旧版CLI
     */
    ZERO(0),

    /**
     * 新版CLI
     */
    ONE(1);

    CliVersion(int i) {
    }

    public static CliVersion valueOf2(Integer version) {
        switch (version) {
            case 0:
                return ZERO;
            case 1:
                return ONE;
            default:
                return ZERO;
        }
    }
}
