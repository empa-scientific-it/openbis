select assert_equals('Number of experiment attachments', 4, count(*)) from experiment_attachments;
select assert_equals('Number of experiment attachment contents', 4, count(*)) from experiment_attachment_contents;
select assert_equals('Number of joined experimnent attachments', 4, count(*)) from experiment_attachments ea, experiment_attachment_contents eac where ea.exac_id = eac.id;