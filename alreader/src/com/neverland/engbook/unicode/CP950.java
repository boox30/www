package com.neverland.engbook.unicode;

import com.neverland.engbook.util.AlStyles;

public class CP950 {
	public static final char getChar(char s1, char s2) {
		char wc = 0x00;
			
		switch (s1) {
		/*case 0x81: if (s2 >= 0x40 && s2 <= 0xfe) wc = CP950Data80.data_81_40_FE[s2 - 0x40]; break;
		case 0x82: if (s2 >= 0x40 && s2 <= 0xfe) wc = CP950Data80.data_82_40_FE[s2 - 0x40]; break;	
		case 0x83: if (s2 >= 0x40 && s2 <= 0xfe) wc = CP950Data80.data_83_40_FE[s2 - 0x40]; break;
		case 0x84: if (s2 >= 0x40 && s2 <= 0xfe) wc = CP950Data80.data_84_40_FE[s2 - 0x40]; break;	
		case 0x85: if (s2 >= 0x40 && s2 <= 0xfe) wc = CP950Data80.data_85_40_FE[s2 - 0x40]; break;
		case 0x86: if (s2 >= 0x40 && s2 <= 0xfe) wc = CP950Data80.data_86_40_FE[s2 - 0x40]; break;	
		case 0x87: if (s2 >= 0x40 && s2 <= 0xfe) wc = CP950Data80.data_87_40_FE[s2 - 0x40]; break;
		case 0x88: if (s2 >= 0x40 && s2 <= 0xfe) wc = CP950Data80.data_88_40_FE[s2 - 0x40]; break;	
		case 0x89: if (s2 >= 0x40 && s2 <= 0xfe) wc = CP950Data80.data_89_40_FE[s2 - 0x40]; break;
		case 0x8a: if (s2 >= 0x40 && s2 <= 0xfe) wc = CP950Data80.data_8A_40_FE[s2 - 0x40]; break;	
		case 0x8b: if (s2 >= 0x40 && s2 <= 0xfe) wc = CP950Data80.data_8B_40_FE[s2 - 0x40]; break;	
		case 0x8c: if (s2 >= 0x40 && s2 <= 0xfe) wc = CP950Data80.data_8C_40_FE[s2 - 0x40]; break;
		case 0x8d: if (s2 >= 0x40 && s2 <= 0xfe) wc = CP950Data80.data_8D_40_FE[s2 - 0x40]; break;	
		case 0x8e: if (s2 >= 0x40 && s2 <= 0xfe) wc = CP950Data80.data_8E_40_FE[s2 - 0x40]; break;
		case 0x8f: if (s2 >= 0x40 && s2 <= 0xfe) wc = CP950Data80.data_8F_40_FE[s2 - 0x40]; break;
		case 0x90: if (s2 >= 0x40 && s2 <= 0xfe) wc = CP950Data80.data_90_40_FE[s2 - 0x40]; break;
		case 0x91: if (s2 >= 0x40 && s2 <= 0xfe) wc = CP950Data80.data_91_40_FE[s2 - 0x40]; break;
		case 0x92: if (s2 >= 0x40 && s2 <= 0xfe) wc = CP950Data80.data_92_40_FE[s2 - 0x40]; break;	
		case 0x93: if (s2 >= 0x40 && s2 <= 0xfe) wc = CP950Data80.data_93_40_FE[s2 - 0x40]; break;
		case 0x94: if (s2 >= 0x40 && s2 <= 0xfe) wc = CP950Data80.data_94_40_FE[s2 - 0x40]; break;	
		case 0x95: if (s2 >= 0x40 && s2 <= 0xfe) wc = CP950Data80.data_95_40_FE[s2 - 0x40]; break;
		case 0x96: if (s2 >= 0x40 && s2 <= 0xfe) wc = CP950Data80.data_96_40_FE[s2 - 0x40]; break;	
		case 0x97: if (s2 >= 0x40 && s2 <= 0xfe) wc = CP950Data80.data_97_40_FE[s2 - 0x40]; break;
		case 0x98: if (s2 >= 0x40 && s2 <= 0xfe) wc = CP950Data80.data_98_40_FE[s2 - 0x40]; break;	
		case 0x99: if (s2 >= 0x40 && s2 <= 0xfe) wc = CP950Data80.data_99_40_FE[s2 - 0x40]; break;
		case 0x9a: if (s2 >= 0x40 && s2 <= 0xfe) wc = CP950Data80.data_9A_40_FE[s2 - 0x40]; break;	
		case 0x9b: if (s2 >= 0x40 && s2 <= 0xfe) wc = CP950Data80.data_9B_40_FE[s2 - 0x40]; break;	
		case 0x9c: if (s2 >= 0x40 && s2 <= 0xfe) wc = CP950Data80.data_9C_40_FE[s2 - 0x40]; break;
		case 0x9d: if (s2 >= 0x40 && s2 <= 0xfe) wc = CP950Data80.data_9D_40_FE[s2 - 0x40]; break;	
		case 0x9e: if (s2 >= 0x40 && s2 <= 0xfe) wc = CP950Data80.data_9E_40_FE[s2 - 0x40]; break;
		case 0x9f: if (s2 >= 0x40 && s2 <= 0xfe) wc = CP950Data80.data_9F_40_FE[s2 - 0x40]; break;
		case 0xa0: if (s2 >= 0x40 && s2 <= 0xfe) wc = CP950DataA0.data_A0_40_FE[s2 - 0x40]; break;*/
		case 0xa1: wc = CP950DataA0.data_A1_40_FE[s2 - 0x40]; break;
		case 0xa2: wc = CP950DataA0.data_A2_40_FE[s2 - 0x40]; break;	
		case 0xa3: if (s2 >= 0x40 && s2 <= 0xe1) wc = CP950DataA0.data_A3_40_E1[s2 - 0x40]; break;
		case 0xa4: wc = CP950DataA0.data_A4_40_FE[s2 - 0x40]; break;	
		case 0xa5: wc = CP950DataA0.data_A5_40_FE[s2 - 0x40]; break;
		case 0xa6: wc = CP950DataA0.data_A6_40_FE[s2 - 0x40]; break;	
		case 0xa7: wc = CP950DataA0.data_A7_40_FE[s2 - 0x40]; break;
		case 0xa8: wc = CP950DataA0.data_A8_40_FE[s2 - 0x40]; break;	
		case 0xa9: wc = CP950DataA0.data_A9_40_FE[s2 - 0x40]; break;
		case 0xaa: wc = CP950DataA0.data_AA_40_FE[s2 - 0x40]; break;	
		case 0xab: wc = CP950DataA0.data_AB_40_FE[s2 - 0x40]; break;	
		case 0xac: wc = CP950DataA0.data_AC_40_FE[s2 - 0x40]; break;
		case 0xad: wc = CP950DataA0.data_AD_40_FE[s2 - 0x40]; break;	
		case 0xae: wc = CP950DataA0.data_AE_40_FE[s2 - 0x40]; break;
		case 0xaf: wc = CP950DataA0.data_AF_40_FE[s2 - 0x40]; break;
		case 0xb0: wc = CP950DataA0.data_B0_40_FE[s2 - 0x40]; break;
		case 0xb1: wc = CP950DataA0.data_B1_40_FE[s2 - 0x40]; break;
		case 0xb2: wc = CP950DataA0.data_B2_40_FE[s2 - 0x40]; break;	
		case 0xb3: wc = CP950DataA0.data_B3_40_FE[s2 - 0x40]; break;
		case 0xb4: wc = CP950DataA0.data_B4_40_FE[s2 - 0x40]; break;	
		case 0xb5: wc = CP950DataA0.data_B5_40_FE[s2 - 0x40]; break;
		case 0xb6: wc = CP950DataA0.data_B6_40_FE[s2 - 0x40]; break;	
		case 0xb7: wc = CP950DataA0.data_B7_40_FE[s2 - 0x40]; break;
		case 0xb8: wc = CP950DataA0.data_B8_40_FE[s2 - 0x40]; break;	
		case 0xb9: wc = CP950DataA0.data_B9_40_FE[s2 - 0x40]; break;
		case 0xba: wc = CP950DataA0.data_BA_40_FE[s2 - 0x40]; break;	
		case 0xbb: wc = CP950DataA0.data_BB_40_FE[s2 - 0x40]; break;	
		case 0xbc: wc = CP950DataA0.data_BC_40_FE[s2 - 0x40]; break;
		case 0xbd: wc = CP950DataA0.data_BD_40_FE[s2 - 0x40]; break;	
		case 0xbe: wc = CP950DataA0.data_BE_40_FE[s2 - 0x40]; break;
		case 0xbf: wc = CP950DataA0.data_BF_40_FE[s2 - 0x40]; break;
		case 0xc0: wc = CP950DataC0.data_C0_40_FE[s2 - 0x40]; break;
		case 0xc1: wc = CP950DataC0.data_C1_40_FE[s2 - 0x40]; break;
		case 0xc2: wc = CP950DataC0.data_C2_40_FE[s2 - 0x40]; break;	
		case 0xc3: wc = CP950DataC0.data_C3_40_FE[s2 - 0x40]; break;
		case 0xc4: wc = CP950DataC0.data_C4_40_FE[s2 - 0x40]; break;	
		case 0xc5: wc = CP950DataC0.data_C5_40_FE[s2 - 0x40]; break;
		case 0xc6: if (s2 >= 0x40 && s2 <= 0x7e) wc = CP950DataC0.data_C6_40_7E[s2 - 0x40]; break;	
		//case 0xc7: if (s2 >= 0x40 && s2 <= 0xfe) wc = CP950DataC0.data_C7_40_FE[s2 - 0x40]; break;
		//case 0xc8: if (s2 >= 0x40 && s2 <= 0xfe) wc = CP950DataC0.data_C8_40_FE[s2 - 0x40]; break;	
		case 0xc9: wc = CP950DataC0.data_C9_40_FE[s2 - 0x40]; break;
		case 0xca: wc = CP950DataC0.data_CA_40_FE[s2 - 0x40]; break;	
		case 0xcb: wc = CP950DataC0.data_CB_40_FE[s2 - 0x40]; break;	
		case 0xcc: wc = CP950DataC0.data_CC_40_FE[s2 - 0x40]; break;
		case 0xcd: wc = CP950DataC0.data_CD_40_FE[s2 - 0x40]; break;	
		case 0xce: wc = CP950DataC0.data_CE_40_FE[s2 - 0x40]; break;
		case 0xcf: wc = CP950DataC0.data_CF_40_FE[s2 - 0x40]; break;
		case 0xd0: wc = CP950DataC0.data_D0_40_FE[s2 - 0x40]; break;
		case 0xd1: wc = CP950DataC0.data_D1_40_FE[s2 - 0x40]; break;
		case 0xd2: wc = CP950DataC0.data_D2_40_FE[s2 - 0x40]; break;	
		case 0xd3: wc = CP950DataC0.data_D3_40_FE[s2 - 0x40]; break;
		case 0xd4: wc = CP950DataC0.data_D4_40_FE[s2 - 0x40]; break;	
		case 0xd5: wc = CP950DataC0.data_D5_40_FE[s2 - 0x40]; break;
		case 0xd6: wc = CP950DataC0.data_D6_40_FE[s2 - 0x40]; break;	
		case 0xd7: wc = CP950DataC0.data_D7_40_FE[s2 - 0x40]; break;
		case 0xd8: wc = CP950DataC0.data_D8_40_FE[s2 - 0x40]; break;	
		case 0xd9: wc = CP950DataC0.data_D9_40_FE[s2 - 0x40]; break;
		case 0xda: wc = CP950DataC0.data_DA_40_FE[s2 - 0x40]; break;	
		case 0xdb: wc = CP950DataC0.data_DB_40_FE[s2 - 0x40]; break;	
		case 0xdc: wc = CP950DataC0.data_DC_40_FE[s2 - 0x40]; break;
		case 0xdd: wc = CP950DataC0.data_DD_40_FE[s2 - 0x40]; break;	
		case 0xde: wc = CP950DataC0.data_DE_40_FE[s2 - 0x40]; break;
		case 0xdf: wc = CP950DataC0.data_DF_40_FE[s2 - 0x40]; break;
		case 0xe0: wc = CP950DataE0.data_E0_40_FE[s2 - 0x40]; break;
		case 0xe1: wc = CP950DataE0.data_E1_40_FE[s2 - 0x40]; break;
		case 0xe2: wc = CP950DataE0.data_E2_40_FE[s2 - 0x40]; break;	
		case 0xe3: wc = CP950DataE0.data_E3_40_FE[s2 - 0x40]; break;
		case 0xe4: wc = CP950DataE0.data_E4_40_FE[s2 - 0x40]; break;	
		case 0xe5: wc = CP950DataE0.data_E5_40_FE[s2 - 0x40]; break;
		case 0xe6: wc = CP950DataE0.data_E6_40_FE[s2 - 0x40]; break;	
		case 0xe7: wc = CP950DataE0.data_E7_40_FE[s2 - 0x40]; break;
		case 0xe8: wc = CP950DataE0.data_E8_40_FE[s2 - 0x40]; break;	
		case 0xe9: wc = CP950DataE0.data_E9_40_FE[s2 - 0x40]; break;
		case 0xea: wc = CP950DataE0.data_EA_40_FE[s2 - 0x40]; break;	
		case 0xeb: wc = CP950DataE0.data_EB_40_FE[s2 - 0x40]; break;	
		case 0xec: wc = CP950DataE0.data_EC_40_FE[s2 - 0x40]; break;
		case 0xed: wc = CP950DataE0.data_ED_40_FE[s2 - 0x40]; break;	
		case 0xee: wc = CP950DataE0.data_EE_40_FE[s2 - 0x40]; break;
		case 0xef: wc = CP950DataE0.data_EF_40_FE[s2 - 0x40]; break;
		case 0xf0: wc = CP950DataE0.data_F0_40_FE[s2 - 0x40]; break;
		case 0xf1: wc = CP950DataE0.data_F1_40_FE[s2 - 0x40]; break;
		case 0xf2: wc = CP950DataE0.data_F2_40_FE[s2 - 0x40]; break;	
		case 0xf3: wc = CP950DataE0.data_F3_40_FE[s2 - 0x40]; break;
		case 0xf4: wc = CP950DataE0.data_F4_40_FE[s2 - 0x40]; break;	
		case 0xf5: wc = CP950DataE0.data_F5_40_FE[s2 - 0x40]; break;
		case 0xf6: wc = CP950DataE0.data_F6_40_FE[s2 - 0x40]; break;	
		case 0xf7: wc = CP950DataE0.data_F7_40_FE[s2 - 0x40]; break;
		case 0xf8: wc = CP950DataE0.data_F8_40_FE[s2 - 0x40]; break;	
		case 0xf9: wc = CP950DataE0.data_F9_40_FE[s2 - 0x40]; break;
		/*case 0xfa: if (s2 >= 0x40 && s2 <= 0xfe) wc = CP950DataE0.data_FA_40_FE[s2 - 0x40]; break;	
		case 0xfb: if (s2 >= 0x40 && s2 <= 0xfe) wc = CP950DataE0.data_FB_40_FE[s2 - 0x40]; break;	
		case 0xfc: if (s2 >= 0x40 && s2 <= 0xfe) wc = CP950DataE0.data_FC_40_FE[s2 - 0x40]; break;
		case 0xfd: if (s2 >= 0x40 && s2 <= 0xfe) wc = CP950DataE0.data_FD_40_FE[s2 - 0x40]; break;	
		case 0xfe: if (s2 >= 0x40 && s2 <= 0xfe) wc = CP950DataE0.data_FE_40_FE[s2 - 0x40]; break;
		case 0xff: if (s2 >= 0x40 && s2 <= 0xfe) wc = CP950DataE0.data_FF_40_FE[s2 - 0x40]; break;*/
		}
		
		if ((wc & AlStyles.STYLE_BASE_MASK) == AlStyles.STYLE_BASE0)
			wc = 0x00;
		
		return wc;
	}
}
