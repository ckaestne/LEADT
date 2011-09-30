MobileMedia : Media_Selection+ [Media_Management] [Hacks] :: _MobileMedia ;

Media_Selection : View_Photo
	| Play_Music ;

Media_Management : [Favourites] [Count_and_Sort] [Copy_Media] [SMS_Transfer] :: _Media_Management ;

Hacks : [Single_Media_Mode] [SMS_or_Copy] [Not_Favourites] [Not_SMS_Transfer] :: _Hacks ;

%%

Single_Media_Mode iff not (View_Photo and Play_Music) and (View_Photo or Play_Music) ;
SMS_or_Copy iff SMS_Transfer or Copy_Media ;
Favourites iff not Not_Favourites ;
SMS_Transfer iff not Not_SMS_Transfer;

