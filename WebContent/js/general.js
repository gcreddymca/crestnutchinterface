/*---------------------------------------------------------------------*/
;(function($){
/*---------------------------------------------------------------------*/
$(document).ready(function(){
/*---------------------------------------------------------------------*/
	//Table Data
	if($('.tableData').length > 0) {
		$('.tableData').each(function(){
			$(this).find('dl').each(function(){
				$(this).find('dd:first').addClass('firstTd');
				$(this).find('dt:first').addClass('firstTh');
				$(this).find('dt:last').addClass('lastTh');
			});
			$(this).find('dl:last').addClass('lastTr');
			$(this).find('dl:even').addClass('evenRow');
			$(this).find('dl:nth-child(2)').find('dt:first').removeClass('firstTh');
		});
	};
	
	//Tooltip
	if($('.tooltip').length > 0){
		$('.tooltip').tooltipster();
	};
	
	//Tabs
	if($('.tabs').length > 0){
		$('.tabContent').hide(); 
		$(".tabs li:first").addClass("active").show();
		$(".tabContent:first").show();

		$('.tabs li').click(function() {
			$('.tabs li').removeClass("active");
			$(this).addClass("active");
			$('.tabContent').hide();
			var activeTab = $(this).find("a").attr("href");
			$(activeTab).fadeIn();
			return false;
		});
	}
	
});
/*---------------------------------------------------------------------*/
})(jQuery);
